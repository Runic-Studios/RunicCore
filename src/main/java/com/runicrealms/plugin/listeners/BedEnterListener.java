package com.runicrealms.plugin.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BedEnterListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBedInteract(PlayerInteractEvent event) {
        Material blockType = event.getClickedBlock() != null ? event.getClickedBlock().getType() : null;
        if (blockType != null && blockType.name().contains("BED")) {
            // Prevent interacting with beds
            event.setCancelled(true);
        }
    }

}
