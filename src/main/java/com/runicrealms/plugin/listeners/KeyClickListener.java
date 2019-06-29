package com.runicrealms.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class KeyClickListener implements Listener {

    /**
     * Stops player from keyboard clicking
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onKeyClick(InventoryClickEvent e) {
        if (e.getClick() == ClickType.NUMBER_KEY ) {
            e.setCancelled(true);
        }
    }
}
