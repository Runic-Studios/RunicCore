package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.item.shops.RunicItemShopHelper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * This method clears 'dungeon keys' from player inventories when they leave the dungeon world
 *
 * @author Skyfallin_
 */
public class WorldChangeListener implements Listener {

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!event.getFrom().getName().equalsIgnoreCase("dungeons")) {
            return;
        }

        RunicItemShopHelper.clearDungeonItems(event.getPlayer());
    }
}