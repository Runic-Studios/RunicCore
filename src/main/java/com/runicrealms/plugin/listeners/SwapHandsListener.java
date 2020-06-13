package com.runicrealms.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SwapHandsListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHandSwap(PlayerSwapHandItemsEvent e) {
        e.setCancelled(true);
    }
}
