package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/*
 * Saves player data async when they logout and removes them from the queue
 */
public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onLoadedQuit(CharacterQuitEvent e) {

        // get player cache (if they've loaded in)
        if (RunicCore.getCacheManager().getPlayerCaches().get(e.getPlayer()) == null) return;

        PlayerCache playerCache = RunicCore.getCacheManager().getPlayerCaches().get(e.getPlayer());
        UUID cacheID = playerCache.getPlayerID();

        RunicCore.getCacheManager().getPlayerCaches().remove(e.getPlayer());

        // remove player data from data queue, remove them from memory
        RunicCore.getCacheManager().getQueuedCaches().removeIf(n -> (n.getPlayerID() == cacheID));

        // update cache, save it
        RunicCore.getCacheManager().savePlayerCache(playerCache, false);
        RunicCore.getCacheManager().setFieldsSaveFile(playerCache, e.getPlayer(), true);

    }

    @EventHandler
    public void onQuit (PlayerQuitEvent e) {
        Player pl = e.getPlayer();
        e.setQuitMessage("");
        pl.setWalkSpeed(0.2f);
    }
}
