package com.runicrealms.plugin.player.cache;

import java.util.HashSet;

public class CacheManager {

    private HashSet<PlayerCache> playerCaches;

    public CacheManager() {
        this.playerCaches = new HashSet<>();
        // save task
    }

    /**
     * Takes information stored in a player cache and writes it to config.
     */
    private void saveCache() {

    }

    public HashSet<PlayerCache> getPlayerCaches() {
        return playerCaches;
    }
}
