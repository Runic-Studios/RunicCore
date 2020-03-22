package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.runiccharacters.api.RunicCharactersApi;
import com.runicrealms.runiccharacters.api.events.CharacterQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onLoadedQuit(CharacterQuitEvent e) {
        // get player cache (if they've loaded in)
        Player pl = e.getPlayer();
        if (RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()) != null) {
            PlayerCache playerCache = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId());

            // update cache
            RunicCore.getCacheManager().savePlayerCache(playerCache);
            RunicCore.getCacheManager().getPlayerCaches().remove(playerCache);
        }
    }

    @EventHandler
    public void onQuit (PlayerQuitEvent e) {
        Player pl = e.getPlayer();
        e.setQuitMessage("");
        pl.setWalkSpeed(0.2f);
        // remove player from RunicCharacters
        RunicCharactersApi.getUserCollection().removePlayer(pl.getUniqueId());
    }
}
