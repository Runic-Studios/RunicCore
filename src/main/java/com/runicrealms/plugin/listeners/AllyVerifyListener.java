package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.api.event.AllyVerifyEvent;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AllyVerifyListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL) // "middle" priority
    public void onAllyVerify(AllyVerifyEvent event) {

        Entity ally = event.getRecipient();

        // target must be a player
        if (!(ally instanceof Player playerAlly)) {
            event.setCancelled(true);
            return;
        }

        // ignore NPCs
        if (playerAlly.hasMetadata("NPC")) {
            event.setCancelled(true);
            return;
        }
        // probably unnecessary, but insurance
        if (playerAlly instanceof ArmorStand) {
            event.setCancelled(true);
        }
    }
}
