package com.runicrealms.plugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class HorseFeedListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Horse
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.APPLE) {
            event.setCancelled(true);
        }
    }

}
