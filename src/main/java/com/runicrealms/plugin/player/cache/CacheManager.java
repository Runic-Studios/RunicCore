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
                Bukkit.broadcastMessage("SAVING PLAYER CACHESS");
                saveCaches();
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 200L, 20*20); // 10s delay, 3 mins
    }

//    // todo: is this even needed? maybe just for a few things? also, mov it to player join listener
//    @EventHandler
//    public void onCharacterLoad(CharacterLoadEvent e) {
//        // method to set all player's info from the player cache object.
//        // remove the methods that do this in mana manager, outlaw manager, etc.
//        // also, switch guilds and professions order in PlayerCache to match scoreboard.
//    }

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
    public void savePlayerCache(PlayerCache playerCache) {
        UserConfig userConfig = RunicCharactersApi.getUserConfig(playerCache.getPlayerID());
        int characterSlot = RunicCharactersApi.getCurrentCharacterSlot(playerCache.getPlayerID());
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
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".currentHP", playerCache.getCurrentHealth());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".maxMana", playerCache.getMaxMana());
        // outlaw
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".outlaw.enabled", playerCache.getIsOutlaw());
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".outlaw.rating", playerCache.getRating());
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
