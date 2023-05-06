package com.runicrealms.plugin.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class CampfireListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CAMPFIRE) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
                if (itemInHand.getType().isEdible()) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
}
