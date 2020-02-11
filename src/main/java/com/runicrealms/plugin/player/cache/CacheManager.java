package com.runicrealms.plugin.player.cache;

import java.util.HashSet;
import java.util.UUID;

public class CacheManager {

    private HashSet<PlayerCache> playerCaches;

    public CacheManager() {
        this.playerCaches = new HashSet<>();
        // save task
    }

    /**
     * Takes information stored in a player cache and writes it to config.
     */
    private void saveCaches() {

    }

    public void savePlayerCache(PlayerCache cache) {
        // write to RunicCharacters
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
}
