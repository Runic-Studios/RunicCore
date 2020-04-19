package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runiccharacters.api.events.CharacterQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Saves player data async
 */
public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onLoadedQuit(CharacterQuitEvent e) {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(),
                () -> saveCharacterRemoveQueue(e.getPlayer()), 1L);
    }

    private void saveCharacterRemoveQueue(Player pl) {
        // get player cache (if they've loaded in)
        if (RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()) != null) {

            PlayerCache playerCache = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId());
            UUID cacheID = playerCache.getPlayerID();

            // remove player data from data queue!
            RunicCore.getCacheManager().getQueuedCaches().removeIf(n -> (n.getPlayerID() == cacheID));

            // update cache, save it
            RunicCore.getCacheManager().savePlayerCache(playerCache, false);
            RunicCore.getCacheManager().setFieldsSaveFile(playerCache, RunicCharactersApi.getUserConfig(pl.getUniqueId()));

            // remove them from cached queue
            RunicCore.getCacheManager().getPlayerCaches().remove(playerCache);

            // remove player from RunicCharacters
            RunicCharactersApi.getUserCollection().removePlayer(pl.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit (PlayerQuitEvent e) {
        Player pl = e.getPlayer();
        e.setQuitMessage("");
        pl.setWalkSpeed(0.2f);
    }
}
