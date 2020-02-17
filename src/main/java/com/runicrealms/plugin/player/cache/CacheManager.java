package com.runicrealms.plugin.player.cache;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runiccharacters.config.UserConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public class CacheManager implements Listener {

    private HashSet<PlayerCache> playerCaches;

    public CacheManager() {
        this.playerCaches = new HashSet<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                //Bukkit.broadcastMessage("SAVING PLAYER CACHESS");
                saveCaches();
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 100L, 30*20); // 10s delay, 30 sec period
    }

    /**
     * Takes information stored in a player cache and writes it to config in RunicCharacters
     */
    private void saveCaches() {
        for (PlayerCache playerCache : playerCaches) {
            UserConfig userConfig = RunicCharactersApi.getUserConfig(playerCache.getPlayerID());
            int characterSlot = RunicCharactersApi.getCurrentCharacterSlot(playerCache.getPlayerID());
            saveFields(playerCache, userConfig, characterSlot);
            userConfig.saveConfig();
        }
    }

    /**
     * To be used during logout
     */
    // todo: broken getting character slot
    public void savePlayerCache(PlayerCache playerCache) {
        UserConfig userConfig = RunicCharactersApi.getUserConfig(playerCache.getPlayerID());
        int characterSlot = userConfig.getCharacterSlot();
        saveFields(playerCache, userConfig, characterSlot);
        userConfig.saveConfig();
    }

    private void saveFields(PlayerCache playerCache, UserConfig userConfig, int characterSlot) {
        // class
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".class.name", playerCache.getClassName());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".class.level", playerCache.getClassLevel());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".class.exp", playerCache.getClassExp());
        // profession
        // todo: add hunter fields
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".prof.name", playerCache.getProfName());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".prof.level", playerCache.getProfLevel());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".prof.exp", playerCache.getProfExp());
        // guild
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".guild", playerCache.getGuild());
        // stats
        playerCache.setCurrentHealth((int) Bukkit.getPlayer(playerCache.getPlayerID()).getHealth());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".currentHP", playerCache.getCurrentHealth());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".maxMana", playerCache.getMaxMana());
        // outlaw
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".outlaw.enabled", playerCache.getIsOutlaw());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".outlaw.rating", playerCache.getRating());
        // inventory
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".inventory", playerCache.getInventoryContents());
        // location
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".location", playerCache.getLocation());
    }

    public HashSet<PlayerCache> getPlayerCaches() {
        return playerCaches;
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
