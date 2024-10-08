package com.runicrealms.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST) // fires early
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DEFAULT) {
            event.setCancelled(true);
        }
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL) {
            event.setCancelled(true);
        }
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.JOCKEY) {
            event.setCancelled(true);
        }
    }
}
