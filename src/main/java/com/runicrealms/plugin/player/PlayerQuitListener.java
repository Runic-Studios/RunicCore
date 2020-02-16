package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit (PlayerQuitEvent e) {

        Player pl = e.getPlayer();

        // remove leave message
        e.setQuitMessage("");

        // make sure the player's walk speed is reset
        pl.setWalkSpeed(0.2f);

        // save player hp
        PlayerCache playerCache = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId());
        playerCache.setCurrentHealth((int) pl.getHealth());
        RunicCore.getCacheManager().savePlayerCache(playerCache);
    }
}
