package com.runicrealms.plugin.player.cache;

import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.database.MongoDataSection;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CacheManager implements Listener {

    private final HashSet<Player> loadedPlayers;
    private final HashSet<PlayerCache> playerCaches;

    private final ConcurrentLinkedQueue<PlayerCache> queuedCaches;
    private static final int CACHE_PERIOD = 30;
    private static final int SAVE_PERIOD = 15;

    public CacheManager() {

        loadedPlayers = new HashSet<>();
        playerCaches = new HashSet<>();
        queuedCaches = new ConcurrentLinkedQueue<>();

        new BukkitRunnable() {
            @Override
            public void run() {
                saveCaches();
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 100L, CACHE_PERIOD*20); // 10s delay, 30 sec period

        new BukkitRunnable() {
            @Override
            public void run() {
                saveQueuedFiles(true, true);
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), (100+CACHE_PERIOD), SAVE_PERIOD*20); // wait for save, 15 sec period
    }

    @EventHandler
    public void onLoad(CharacterLoadEvent e) {
        loadedPlayers.add(Bukkit.getPlayer(e.getPlayerCache().getPlayerID()));
    }

    @EventHandler
    public void onQuit(CharacterQuitEvent e) {
        loadedPlayers.remove(e.getPlayer());
    }

    /**
     * Takes information stored in a player cache and writes it to config in RunicCharacters
     */
    public void saveCaches() {
        for (PlayerCache playerCache : playerCaches) {
            savePlayerCache(playerCache, true);
        }
    }

    /**
     * Also used on server disable and logout. Updates information about player and adds them to data queue for saving.
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

    /**
     * Writes data async
     */
    public void saveQueuedFiles(boolean limitSize, boolean saveAsync) {
        int limit;
        if (limitSize) {
            limit = (int) Math.ceil(queuedCaches.size() / 4.0);
        } else {
            limit = queuedCaches.size();
        }
        if (limit < 1)
            return;
        for (int i = 0; i < limit; i++) {
            if (queuedCaches.size() < 1)
                continue;
            PlayerCache queued = queuedCaches.iterator().next();
            setFieldsSaveFile(queued, Bukkit.getPlayer(queued.getPlayerID()), saveAsync);
            queuedCaches.remove(queued);
        }
    }

    public void setFieldsSaveFile(PlayerCache playerCache, Player player, boolean saveAsync) {
        try {
            int slot = playerCache.getCharacterSlot();
            PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
            PlayerMongoDataSection character = mongoData.getCharacter(slot);
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
            // outlaw
            character.set("outlaw.enabled", playerCache.getIsOutlaw());
            character.set("outlaw.rating", playerCache.getRating());
            // inventory
            character.set("inventory", DatabaseUtil.serializeInventory(player.getInventory()));
            // location
            character.set("location", DatabaseUtil.serializeLocation(playerCache.getLocation()));
            // save data (includes nested fields)
            if (saveAsync) {
                Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), mongoData::save);
            } else {
                mongoData.save();
            }
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: Data of player cache to save was null.");
            e.printStackTrace();
        }
    }

    public HashSet<Player> getLoadedPlayers() {
        return loadedPlayers;
    }

    public HashSet<PlayerCache> getPlayerCaches() {
        return playerCaches;
    }

    public ConcurrentLinkedQueue<PlayerCache> getQueuedCaches() {
        return queuedCaches;
    }

    /**
     * Grab the cache of a particular player
     */
    public PlayerCache getPlayerCache(UUID playerID) {
        for (PlayerCache cache : playerCaches) {
            if (cache.getPlayerID() == playerID) return cache;
        }
        return null;
    }

    /**
     * Check if a player has loaded a character
     */
    public boolean hasCacheLoaded(UUID playerID) {
        for (PlayerCache cache : playerCaches) {
            if (cache.getPlayerID() == playerID) return true;
        }
        return false;
    }

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

        boolean isOutlaw = character.get("outlaw.enabled", Boolean.class);
        int rating = character.get("outlaw.rating", Integer.class);

        ItemStack[] inventoryContents = DatabaseUtil.loadInventory(character.get("inventory", String.class));
        Location location = DatabaseUtil.loadLocation(character.get("location", String.class));

        return new PlayerCache(slot,
                player.getUniqueId(),
                guildName, className, profName,
                classLevel, classExp,
                profLevel, profExp,
                currentHealth, maxMana,
                isOutlaw, rating,
                inventoryContents, location, mongoData);
    }

    /**
     * Call on-join
     */
    public void tryCreateNewPlayer(Player pl) {
        UUID uuid = pl.getUniqueId();
        /*
        If the data file doesn't exist, we're gonna build it
         */
        if (RunicCore.getDatabaseManager().getPlayerData().find
                (Filters.eq("player_uuid", uuid.toString())).first() == null) {
            Document newDataFile = new Document("player_uuid", uuid.toString())
                    .append("guild", "None");
            RunicCore.getDatabaseManager().getPlayerData().insertOne(newDataFile);
        }
    }

    /**
     * Call from RunicCharacters
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
        mongoDataSection.set("outlaw.enabled", false);
        mongoDataSection.set("outlaw.rating", RunicCore.getOutlawManager().getBaseRating());
        mongoDataSection.set("inventory", DatabaseUtil.serializeInventory(new ItemStack[41])); // empty inventory
        mongoDataSection.set("location", DatabaseUtil.serializeLocation(new Location(Bukkit.getWorld("Alterra"), -2317.5, 38.5, 1719.5))); // tutorial
        mongoData.save();
    }
}
