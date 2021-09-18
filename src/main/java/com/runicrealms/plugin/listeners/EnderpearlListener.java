package com.runicrealms.plugin.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * A listener used to make sure players cant throw ender pearls
 *
 * @author BoBoBalloon
 */
public class EnderpearlListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getMaterial() == Material.ENDER_PEARL) {
            event.setCancelled(true);
        }
    }
}
