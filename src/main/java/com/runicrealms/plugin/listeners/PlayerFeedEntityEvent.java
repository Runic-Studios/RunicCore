package com.runicrealms.plugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerFeedEntityEvent implements Listener {

    @EventHandler
    public void onEntityEnterLoveMode(EntityEnterLoveModeEvent event) {
        if (event.getHumanEntity() != null && event.getHumanEntity().getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onFeedHorseEvent(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) return;
        Material material = event.getPlayer().getInventory().getItemInMainHand().getType();
        if (event.getRightClicked().getType() == EntityType.HORSE
                || event.getRightClicked().getType() == EntityType.DONKEY
                || event.getRightClicked().getType() == EntityType.MULE) {
            if (material == Material.GOLDEN_APPLE
                    || material == Material.APPLE
                    || material == Material.ENCHANTED_GOLDEN_APPLE
                    || material == Material.GOLDEN_CARROT
                    || material == Material.SUGAR
                    || material == Material.WHEAT
                    || material == Material.HAY_BLOCK) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getRightClicked().getType() == EntityType.WOLF) {
            if (material == Material.BEEF
                    || material == Material.COOKED_BEEF
                    || material == Material.CHICKEN
                    || material == Material.COOKED_CHICKEN
                    || material == Material.PORKCHOP
                    || material == Material.COOKED_PORKCHOP
                    || material == Material.MUTTON
                    || material == Material.COOKED_MUTTON
                    || material == Material.RABBIT
                    || material == Material.COOKED_RABBIT
                    || material == Material.ROTTEN_FLESH) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getRightClicked().getType() == EntityType.CAT
                || event.getRightClicked().getType() == EntityType.OCELOT) {
            if (material == Material.COD || material == Material.SALMON) {
                event.setCancelled(true);
                return;
            }
        }
        if (event.getRightClicked().getType() == EntityType.LLAMA
                || event.getRightClicked().getType() == EntityType.TRADER_LLAMA) {
            if (material == Material.WHEAT) {
                event.setCancelled(true);
            }
        }
    }

}
