package com.runicrealms.plugin.player.cache;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import com.runicrealms.plugin.database.event.CacheSaveReason;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import com.runicrealms.plugin.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

// todo: rename to sessionManager, save data from PlayerData and CharacterData
public class CacheManager implements Listener {

    private static final int CACHE_PERIOD = 30;

    private final BukkitTask cacheSavingTask;
    private final ConcurrentHashMap<Player, PlayerCache> playerCaches;
    private final ConcurrentLinkedQueue<PlayerCache> queuedCaches;
    private final Map<UUID, PlayerData> playerDataMap;

    /*
    Saves files ASYNC // TODO: sync, only on shutdown
     */
    public CacheManager() {

        playerCaches = new ConcurrentHashMap<>();
        queuedCaches = new ConcurrentLinkedQueue<>();
        this.playerDataMap = new HashMap<>();

        cacheSavingTask = new BukkitRunnable() { // Asynchronously
            @Override
            public void run() {
                saveCaches();
                saveQueuedFiles(true, true, CacheSaveReason.PERIODIC);
            }
        }.runTaskTimer(RunicCore.getInstance(), 100L, CACHE_PERIOD * 20); // 10s delay, 30 sec period
    }

    @EventHandler
    public void onLoad(CharacterLoadEvent e) {
        playerCaches.put(e.getPlayer(), e.getPlayerCache());
    }

    /**
     * Takes information stored in a player cache and writes it to config in RunicCharacters
     */
    public void saveCaches() {
        for (PlayerCache playerCache : playerCaches.values()) {
            savePlayerCache(playerCache, true);
        }
    }

    /**
     * Also used on server disable and logout. Updates information about player and adds them to data queue for saving.
     * IMPORTANT: Inventory and Location and saved here
     *
     * @param playerCache the object which is storing their information
     * @param willQueue   whether to save immediately or queue file for saving
     */
    public void savePlayerCache(PlayerCache playerCache, boolean willQueue) { // could be a /class command
        Player player = Bukkit.getPlayer(playerCache.getPlayerID());
        playerCache.setCurrentHealth((int) player.getHealth()); // update current player hp
        playerCache.setInventoryContents(player.getInventory().getContents()); // update inventory
        playerCache.setLocation(player.getLocation()); // update location
        if (willQueue) {
            queuedCaches.removeIf(n -> (n.getPlayerID() == playerCache.getPlayerID())); // prevent duplicates
            queuedCaches.add(playerCache); // queue the file for saving
        }
    }

    public void saveAllCachedPlayers(CacheSaveReason cacheSaveReason) {
        for (PlayerCache playerCache : playerCaches.values()) {
            setFieldsSaveFile(playerCache, Bukkit.getPlayer(playerCache.getPlayerID()), false, cacheSaveReason);
        }
    }

    /**
     * Generally saves files async unless specified otherwise
     *
     * @param limitSize       manually "squeeze" the amount of files that may be saved at a time to guarantee performance
     * @param saveAsync       whether to save the data async (true in most cases, unless shutdown)
     * @param cacheSaveReason the method it's using to save (running task, logout, shutdown, etc.)
     */
    public void saveQueuedFiles(boolean limitSize, boolean saveAsync, CacheSaveReason cacheSaveReason) {
        int limit;
        if (limitSize) {
            limit = (int) Math.ceil(queuedCaches.size() / 4.0);
        } else {
            limit = queuedCaches.size();
        }
        if (limit < 1)
            return;
        for (int i = 0; i < limit; i++) {
            if (queuedCaches.size() < 1) continue;
            PlayerCache queued = queuedCaches.iterator().next();
            setFieldsSaveFile(queued, Bukkit.getPlayer(queued.getPlayerID()), saveAsync, cacheSaveReason);
            queuedCaches.remove(queued);
        }
    }

    /**
     * This is our main method for updating a player's database document in Atlas
     *
     * @param playerCache     the object which is storing their information
     * @param player          player to save data for
     * @param saveAsync       whether to save the data async (true in most cases, unless shutdown)
     * @param cacheSaveReason the method it's using to save (running task, logout, shutdown, etc.)
     */
    public void setFieldsSaveFile(PlayerCache playerCache, Player player, boolean saveAsync,
                                  CacheSaveReason cacheSaveReason) {
        try {
            int slot = playerCache.getCharacterSlot();
            PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
            mongoData.set("last_login", LocalDate.now());
            PlayerMongoDataSection character = mongoData.getCharacter(slot);
            CacheSaveEvent e = new CacheSaveEvent(player, mongoData, character, cacheSaveReason);
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled()) return;
            if (playerCache.getClassName() != null)
                character.set("class.name", playerCache.getClassName());
            character.set("class.level", playerCache.getClassLevel());
            character.set("class.exp", playerCache.getClassExp());
            // guild
            if (playerCache.getGuild() != null)
                mongoData.set("guild", playerCache.getGuild());
            // profession
            if (playerCache.getProfName() != null)
                character.set("prof.name", playerCache.getProfName());
            character.set("prof.level", playerCache.getProfLevel());
            character.set("prof.exp", playerCache.getProfExp());
            // stats (health is updated above)
            character.set("currentHP", playerCache.getCurrentHealth());
            character.set("maxMana", playerCache.getMaxMana());
            character.set("storedHunger", player.getFoodLevel());
            // outlaw
            character.set("outlaw.enabled", playerCache.getIsOutlaw());
            character.set("outlaw.rating", playerCache.getRating());
            // location
            character.remove("location"); // remove old save format
            DatabaseUtil.saveLocation(character, playerCache.getLocation());
            // save data (includes nested fields)
            if (saveAsync)
                Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), mongoData::save);
            else
                mongoData.save();
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: Data of player cache to save was null.");
            e.printStackTrace();
        }
    }

    public BukkitTask getCacheSavingTask() {
        return cacheSavingTask;
    }

    public ConcurrentHashMap.KeySetView<Player, PlayerCache> getLoadedPlayers() {
        return playerCaches.keySet();
    }

    public ConcurrentHashMap<Player, PlayerCache> getPlayerCaches() {
        return playerCaches;
    }

    public ConcurrentLinkedQueue<PlayerCache> getQueuedCaches() {
        return queuedCaches;
    }

    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    /*
     * Check if a player has loaded a character
     */
    public boolean hasCacheLoaded(Player pl) {
        return playerCaches.get(pl) != null;
    }

    /**
     * Restores player info from MongoDB when they load their character
     *
     * @param player to load character for
     * @param slot   of the character to load
     * @return a cache of info with all relevant information
     */
    // TODO: only call this IF the data exists but not in redis
    public PlayerCache buildPlayerCache(Player player, Integer slot) {

        PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
        PlayerMongoDataSection character = mongoData.getCharacter(slot);

        String guildName = mongoData.get("guild", String.class); // account-wide

        String className = character.get("class.name", String.class);
        int classLevel = character.get("class.level", Integer.class);
        int classExp = character.get("class.exp", Integer.class);

        String profName = character.get("prof.name", String.class);
        int profLevel = character.get("prof.level", Integer.class);
        int profExp = character.get("prof.exp", Integer.class);

        int currentHealth = character.get("currentHP", Integer.class);
        int maxMana = character.get("maxMana", Integer.class);
        int storedHunger = character.get("storedHunger", Integer.class) != null ? character.get("storedHunger", Integer.class) : 20;

        boolean isOutlaw = character.get("outlaw.enabled", Boolean.class);
        int rating = character.get("outlaw.rating", Integer.class);

        Location location = DatabaseUtil.loadLocation(player, character);

        return new PlayerCache(slot,
                player.getUniqueId(),
                guildName, className, profName,
                classLevel, classExp,
                profLevel, profExp,
                currentHealth, maxMana, storedHunger,
                isOutlaw, rating,
                /*inventoryContents*/null, location, mongoData);
    }

    /**
     * Builds a new database document for the given player if it doesn't already exist when they join server/lobby
     *
     * @param player who joined
     */
    public void tryCreateNewPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        // Step 1: check if player data is cached in redis
        if (checkRedisForPlayerData(player)) return;
        // Step 2: check mongo documents loaded in memory (last 30 days)
        if (RunicCore.getDatabaseManager().getPlayerDataLastMonth().containsKey(uuid.toString())) return;
        // Step 3: check entire mongo collection
        if (RunicCore.getDatabaseManager().getPlayersDB().getCollection("player_data").find
                (Filters.eq("player_uuid", uuid.toString())).limit(1).first() != null)
            return;
        // Step 4: if no data is found, we create some data, add it to mongo, then store a reference in memory
        RunicCore.getDatabaseManager().addNewDocument(uuid);
    }

    private boolean checkRedisForPlayerData(Player player) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            if (jedis.exists(String.valueOf(player.getUniqueId()))) {
                Bukkit.broadcastMessage("redis data found");
                return true;
            }
        }
        Bukkit.broadcastMessage("redis data not found");
        return false;
    }

    /**
     * Attempts to populate the document for given character and slot with basic values
     *
     * @param player    who created a new character
     * @param className the name of the class
     * @param slot      the slot of the character
     */
    // TODO: move into database manager
    public void loadCharacterData(Player player, String className, Integer slot) {
        // Step 1: check if character data is cached in redis
        if (checkRedisForCharacterData(player, slot)) return;
        // Step 2: check mongo documents loaded in memory (last 30 days)

        // Step 3: check entire mongo collection

        // Step 4: if no data is found, we create some data, add it to mongo, then store a reference in redis
        RunicCore.getDatabaseManager().addNewCharacter(player, className, slot);
    }

    private boolean checkRedisForCharacterData(Player player, Integer slot) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            if (jedis.exists(player.getUniqueId() + ":character:" + slot)) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "redis character data found");
                return true;
            }
        }
        Bukkit.broadcastMessage(ChatColor.RED + "redis character data not found");
        return false;
    }
}
