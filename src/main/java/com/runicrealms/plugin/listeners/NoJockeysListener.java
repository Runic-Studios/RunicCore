package com.runicrealms.plugin.listeners;

import org.bukkit.entity.Chicken;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class NoJockeysListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.getVehicle() != null && entity.getVehicle() instanceof Spider) {
            // This is a Spider Jockey - remove it
            event.setCancelled(true);
        }

        if (entity instanceof Chicken && !entity.getPassengers().isEmpty()) {
            // This is a Chicken Jockey - remove it
            event.setCancelled(true);
        }
    }

}
