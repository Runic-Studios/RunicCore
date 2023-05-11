package com.runicrealms.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class SheepShearListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSheepShear(PlayerShearEntityEvent event) {
        event.setCancelled(true);
    }

}
