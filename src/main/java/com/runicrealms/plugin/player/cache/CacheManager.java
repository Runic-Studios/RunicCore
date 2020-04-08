package com.runicrealms.plugin.player.cache;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.MongoData;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import com.runicrealms.runiccharacters.api.events.CharacterQuitEvent;
import com.runicrealms.runiccharacters.config.UserConfig;
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

    private HashSet<Player> loadedPlayers;
    private HashSet<PlayerCache> playerCaches;

    private volatile ConcurrentLinkedQueue<PlayerCache> queuedCaches;
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
                saveQueuedFiles(true);
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
        UserConfig userConfig = RunicCharactersApi.getUserConfig(playerCache.getPlayerID());
        Player pl = userConfig.getPlayer();
        playerCache.setCurrentHealth((int) pl.getHealth()); // update current player hp
        playerCache.setInventoryContents(pl.getInventory().getContents()); // update inventory
        playerCache.setLocation(pl.getLocation()); // update location
        if (willQueue)
            queuedCaches.add(playerCache); // queue the file for saving
    }

    /**
     * Writes data async
     */
    public void saveQueuedFiles(boolean limitSize) {
        int limit;
        if (limitSize)
            limit = (int) Math.ceil(queuedCaches.size() / 4);
        else
            limit = queuedCaches.size();
        UserConfig userConfig;
        int characterSlot;
        for (int i = 0; i < limit; i++) {
            if (queuedCaches.size() < 1) continue;
            if (!queuedCaches.iterator().hasNext()) continue;
            PlayerCache queued = queuedCaches.iterator().next();
            userConfig = RunicCharactersApi.getUserConfig(queued.getPlayerID());
            characterSlot = queued.getCharacterSlot();
            setFieldsSaveFile(queued, userConfig, characterSlot);
            queuedCaches.remove(queued);
        }
    }

    public void setFieldsSaveFile(PlayerCache playerCache, UserConfig userConfig, int characterSlot) {
        // class
        if (userConfig == null) return;
        if (playerCache.getClassName() != null) {
            userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".class.name", playerCache.getClassName());
        }
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".class.level", playerCache.getClassLevel());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".class.exp", playerCache.getClassExp());
        // guild
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".guild", playerCache.getGuild());
        // profession
        if (playerCache.getProfName() != null) {
            userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".prof.name", playerCache.getProfName());
        }
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".prof.level", playerCache.getProfLevel());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".prof.exp", playerCache.getProfExp());
        // stats
        playerCache.setCurrentHealth((int) Bukkit.getPlayer(playerCache.getPlayerID()).getHealth());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".currentHP", playerCache.getCurrentHealth());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".maxMana", playerCache.getMaxMana());
        // outlaw
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".outlaw.enabled", playerCache.getIsOutlaw());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".outlaw.rating", playerCache.getRating());
        // inventory
        saveInventory(playerCache, userConfig);
        // location
        playerCache.getMongoData().set("character." + userConfig.getCharacterSlot() + ".location", DatabaseUtil.serializedLocation(playerCache.getLocation()));
        //RunicCore.getDatabaseManager().getAPI().getCharacterAPI().updateCharacterLoc(playerCache.getPlayerID().toString(), userConfig.getCharacterSlot(), playerCache.getLocation());
        // save file
        userConfig.saveConfig();
    }

    /**
     * Stores player inventory between alts, ignoring null items. (saves a lot of space)
     * @param userConfig from RunicCharacters
     */
    public void saveInventory(PlayerCache playerCache, UserConfig userConfig) {
        Player pl = userConfig.getPlayer();
        int characterSlot = userConfig.getCharacterSlot();
        playerCache.getMongoData().set("character." + characterSlot + ".inventory", DatabaseUtil.serializeInventory(pl.getInventory()));
        //RunicCore.getDatabaseManager().getAPI().getCharacterAPI().updateCharacterInv
                //(pl.getUniqueId().toString(), characterSlot, pl.getInventory());
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

    public PlayerCache buildPlayerCache(UserConfig userConfig) { // TODO - use mongodata to build player cache

        PlayerMongoData mongoData = new PlayerMongoData(userConfig.getPlayer().getUniqueId().toString());

        String path = userConfig.getCharacterSlot() + "." + UserConfig.getConfigHeader();

        UUID playerID = userConfig.getPlayer().getUniqueId();
        String guildName = userConfig.getConfigurationSection(path).getString("guild");
        String className = userConfig.getConfigurationSection(path).getString("class.name");
        String profName = userConfig.getConfigurationSection(path).getString("prof.name");

        int classLevel = userConfig.getConfigurationSection(path).getInt("class.level");
        int classExp = userConfig.getConfigurationSection(path).getInt("class.exp");
        int profLevel = userConfig.getConfigurationSection(path).getInt("prof.level");
        int profExp = userConfig.getConfigurationSection(path).getInt("prof.exp");

        int currentHealth = userConfig.getConfigurationSection(path).getInt("currentHP");
        int maxMana = userConfig.getConfigurationSection(path).getInt("maxMana");

        boolean isOutlaw = userConfig.getConfigurationSection(path).getBoolean("outlaw.enabled");
        int rating = userConfig.getConfigurationSection(path).getInt("outlaw.rating");

        //ItemStack[] inventoryContents = RunicCore.getCacheManager().loadInventory(userConfig);
        //Location location = (Location) userConfig.getConfigurationSection(path).get("location");

        //ItemStack[] inventoryContents = RunicCore.getCacheManager().loadInventory(userConfig);

        ItemStack[] inventoryContents = DatabaseUtil.loadInventory(mongoData.get("characters." + userConfig.getCharacterSlot() + ".inventory", String.class));
        Location location = DatabaseUtil.loadLocation(userConfig);

        return new PlayerCache(userConfig.getCharacterSlot(),
                playerID, guildName, className, profName,
                classLevel, classExp,
                profLevel, profExp,
                currentHealth, maxMana,
                isOutlaw, rating,
                inventoryContents, location, mongoData);
    }
}
