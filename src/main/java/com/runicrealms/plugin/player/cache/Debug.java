package com.runicrealms.plugin.player.cache;

import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Debug implements Listener {

    @EventHandler
    public void onLoad(CharacterLoadEvent e) {
        displayStuff(e.getPlayerCache());
    }

    public static void displayStuff(PlayerCache playerCache) {
        Bukkit.broadcastMessage(playerCache.getClassName());
        Bukkit.broadcastMessage("" + playerCache.getGuild());
        Bukkit.broadcastMessage("" + playerCache.getProfName());

        Bukkit.broadcastMessage("" + playerCache.getClassLevel());
        Bukkit.broadcastMessage("" + playerCache.getClassExp());
        Bukkit.broadcastMessage("" + playerCache.getProfLevel());
        Bukkit.broadcastMessage("" + playerCache.getProfExp());

        Bukkit.broadcastMessage("" + playerCache.getCurrentHealth());
        Bukkit.broadcastMessage("" + playerCache.getMaxMana());

        Bukkit.broadcastMessage("" + playerCache.getIsOutlaw());
        Bukkit.broadcastMessage("" + playerCache.getRating());

        Bukkit.broadcastMessage("" + playerCache.getInventoryContents()[0]);
        Bukkit.broadcastMessage("" + playerCache.getLocation());
    }
}
