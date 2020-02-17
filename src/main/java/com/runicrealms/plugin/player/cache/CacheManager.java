package com.runicrealms.plugin.player.cache;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runiccharacters.config.UserConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
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
        saveInventory(userConfig);
        // location
        userConfig.set(characterSlot, UserConfig.getConfigHeader() + ".location", playerCache.getLocation());
    }

    /**
     * Stores player inventory between alts, ignoring null items. (saves a lot of space)
     * @param userConfig from RunicCharacters
     */
    public void saveInventory(UserConfig userConfig) {
        ItemStack[] contents = userConfig.getPlayer().getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                ItemStack item = contents[i];
                userConfig.set(userConfig.getCharacterSlot(), UserConfig.getConfigHeader() + ".inventory." + i, item);
            }
        }
    }

    /**
     * Loads inventory from flat file into memory
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
