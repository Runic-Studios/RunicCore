package com.runicrealms.plugin.player.cache;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.MongoDataSection;
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

        PlayerMongoData mongoData = new PlayerMongoData(userConfig.getPlayer().getUniqueId().toString());
        MongoDataSection characterData = mongoData.getCharacter(characterSlot);

        if (playerCache.getClassName() != null) {
            characterData.set("class.name", playerCache.getClassName());
        }
        characterData.set("class.level", playerCache.getClassLevel());
        characterData.set("class.exp", playerCache.getClassExp());
        // guild
        characterData.set("guild", playerCache.getGuild());
        // profession
        if (playerCache.getProfName() != null) {
            characterData.set("prof.name", playerCache.getProfName());
        }
        characterData.set("prof.level", playerCache.getProfLevel());
        characterData.set("prof.exp", playerCache.getProfExp());
        // stats
        playerCache.setCurrentHealth((int) Bukkit.getPlayer(playerCache.getPlayerID()).getHealth());
        characterData.set("currentHP", playerCache.getCurrentHealth());
        characterData.set("maxMana", playerCache.getMaxMana());
        // outlaw
        characterData.set("outlaw.enabled", playerCache.getIsOutlaw());
        characterData.set("outlaw.rating", playerCache.getRating());
        // inventory
        saveInventory(playerCache, userConfig);
        // location
        characterData.set("location", DatabaseUtil.serializeLocation(playerCache.getLocation()));
        // save data (includes nested fields)
        playerCache.getMongoData().save();
    }

    /**
     * Stores player inventory between alts, ignoring null items. (saves a lot of space)
     * @param userConfig from RunicCharacters
     */
    public void saveInventory(PlayerCache playerCache, UserConfig userConfig) {
        Player pl = userConfig.getPlayer();
        int characterSlot = userConfig.getCharacterSlot();
        playerCache.getMongoData().set("character." + characterSlot + ".inventory", DatabaseUtil.serializeInventory(pl.getInventory()));
        playerCache.getMongoData().save();
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

    public PlayerCache buildPlayerCache(UserConfig userConfig) {

        int slot = userConfig.getCharacterSlot();
        PlayerMongoData mongoData = new PlayerMongoData(userConfig.getPlayer().getUniqueId().toString());
        MongoDataSection characterData = mongoData.getCharacter(slot);

        String guildName = mongoData.get("guild", String.class); // account-wide
        String className = characterData.get("class.name", String.class);
        String profName = characterData.get("prof.name", String.class);

        int classLevel = characterData.get("class.level", Integer.class);
        int classExp = characterData.get("class.exp", Integer.class);
        int profLevel = characterData.get("prof.level", Integer.class);
        int profExp = characterData.get("prof.exp", Integer.class);

        int currentHealth = characterData.get("currentHP", Integer.class);
        int maxMana = characterData.get("maxMana", Integer.class);

        boolean isOutlaw = characterData.get("outlaw.enabled", Boolean.class);
        int rating = characterData.get("outlaw.rating", Integer.class);

        ItemStack[] inventoryContents = DatabaseUtil.loadInventory(characterData.get("inventory", String.class));
        Location location = DatabaseUtil.loadLocation(characterData.get("location", String.class));

        return new PlayerCache(userConfig.getCharacterSlot(),
                userConfig.getPlayer().getUniqueId(),
                guildName, className, profName,
                classLevel, classExp,
                profLevel, profExp,
                currentHealth, maxMana,
                isOutlaw, rating,
                inventoryContents, location, mongoData);
    }
}
