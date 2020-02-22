package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.runiccharacters.api.RunicCharactersApi;
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

        // get player cache
        PlayerCache playerCache = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId());

        // update cache
        RunicCore.getCacheManager().savePlayerCache(playerCache);

        // remove player from RunicCharacters
        RunicCharactersApi.getUserCollection().removePlayer(pl.getUniqueId());
        RunicCore.getCacheManager().getPlayerCaches().remove(playerCache);
    }
}
