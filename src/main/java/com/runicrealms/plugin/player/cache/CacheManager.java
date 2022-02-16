package com.runicrealms.plugin.player.cache;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import com.runicrealms.plugin.database.event.CacheSaveReason;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.utilities.HearthstoneItemUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CacheManager implements Listener {

    private static final int CACHE_PERIOD = 30;

    private final BukkitTask cacheSavingTask;
    private final ConcurrentHashMap<Player, PlayerCache> playerCaches;
    private final ConcurrentLinkedQueue<PlayerCache> queuedCaches;

    /*
    Saves files ASYNC
     */
    public CacheManager() {

        playerCaches = new ConcurrentHashMap<>();
        queuedCaches = new ConcurrentLinkedQueue<>();

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
        Player pl = Bukkit.getPlayer(playerCache.getPlayerID());
        playerCache.setCurrentHealth((int) pl.getHealth()); // update current player hp
        playerCache.setInventoryContents(pl.getInventory().getContents()); // update inventory
        playerCache.setLocation(pl.getLocation()); // update location
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
    public PlayerCache buildPlayerCache(Player player, Integer slot) {

        PlayerMongoData mongoData = new PlayerMongoData(player.getPlayer().getUniqueId().toString());
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
        /*
        If the data file doesn't exist, we're going to build it
         */
        if (RunicCore.getDatabaseManager().getPlayerData().find
                (Filters.eq("player_uuid", uuid.toString())).first() == null) {
            Document newDataFile = new Document("player_uuid", uuid.toString())
                    .append("guild", "None");
            RunicCore.getDatabaseManager().getPlayerData().insertOne(newDataFile);
        }
    }

    /**
     * Attempts to populate the document for given character and slot with basic values
     *
     * @param player    who created a new character
     * @param className the name of the class
     * @param slot      the slot of the character
     */
    public void tryCreateNewCharacter(Player player, String className, Integer slot) {
        PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
        MongoDataSection mongoDataSection = mongoData.getSection("character." + slot);
        mongoDataSection.set("class.name", className);
        mongoDataSection.set("class.level", 0);
        mongoDataSection.set("class.exp", 0);
        mongoDataSection.set("prof.name", "None");
        mongoDataSection.set("prof.level", 0);
        mongoDataSection.set("prof.exp", 0);
        mongoDataSection.set("currentHP", HealthUtils.getBaseHealth());
        mongoDataSection.set("maxMana", RunicCore.getRegenManager().getBaseMana());
        mongoDataSection.set("storedHunger", 20);
        mongoDataSection.set("outlaw.enabled", false);
        mongoDataSection.set("outlaw.rating", RunicCore.getBaseOutlawRating());
        DatabaseUtil.saveLocation(mongoData.getCharacter(slot), CityLocation.getLocationFromItemStack(HearthstoneItemUtil.HEARTHSTONE_ITEMSTACK)); // tutorial fortress
        mongoData.save();
    }
}
