package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.common.event.RunicTeleportEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class RunicTeleportListener implements Listener {

    /**
     * Listener to apply cleanup before a player is teleported
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRunicTeleport(RunicTeleportEvent event) {
        List<Entity> passengers = event.getPlayer().getPassengers();
        passengers.forEach(passenger -> event.getPlayer().removePassenger(passenger));
        event.getPlayer().teleport(event.getLocation());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        List<Entity> passengers = event.getPlayer().getPassengers();
        passengers.forEach(passenger -> event.getPlayer().removePassenger(passenger));
    }
}
