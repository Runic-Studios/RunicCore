package com.runicrealms.plugin.player.cache;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import com.runicrealms.runiccharacters.api.events.CharacterQuitEvent;
import com.runicrealms.runiccharacters.config.UserConfig;
import org.bukkit.Bukkit;
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
        saveInventory(userConfig);
        // location
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".location", playerCache.getLocation());
        // save file
        userConfig.saveConfig();
    }

    /**
     * Stores player inventory between alts, ignoring null items. (saves a lot of space)
     * @param userConfig from RunicCharacters
     */
    public void saveInventory(UserConfig userConfig) {
        userConfig.set(userConfig.getCharacterSlot(), UserConfig.getConfigHeader() + ".inventory", null);
        ItemStack[] contents = userConfig.getPlayer().getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                ItemStack item = contents[i];
                userConfig.set(userConfig.getCharacterSlot(), UserConfig.getConfigHeader() + ".inventory." + i, item);
            }
        }
    }

    /**
     * Loads inventory from flat file into memory.
     * Called in RunicCharacters.
     * @param userConfig from RunicCharacters
     */
    public ItemStack[] loadInventory(UserConfig userConfig) {
        ItemStack[] contents = new ItemStack[41];
        for (int i = 0; i < contents.length; i++) {
            if (userConfig.getConfigurationSection(userConfig.getCharacterSlot() + "." + UserConfig.getConfigHeader()).getItemStack("inventory." + i) != null) {
                ItemStack item = userConfig.getConfigurationSection(userConfig.getCharacterSlot() + "." + UserConfig.getConfigHeader() + ".inventory").getItemStack(i + "");
                contents[i] = item;
            }
        }
        return contents;
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
}
