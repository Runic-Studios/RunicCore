package com.runicrealms.plugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;

public class BlockInteractListener implements Listener {

    private static final Set<Material> BLOCKED_TYPES = new HashSet<>() {{
        add(Material.BELL);
        add(Material.JUKEBOX);
        add(Material.NOTE_BLOCK);
        add(Material.LODESTONE);
    }};

    @EventHandler
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onEnterBed(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    /**
     * Prevents destruction of soil, interaction with tons of new blocks
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL
                && event.getClickedBlock() != null
                && (event.getClickedBlock().getType() == Material.FARMLAND || event.getClickedBlock().getType() == Material.LEGACY_SOIL)) {
            event.setCancelled(true);
            return;
        }
        if (event.getClickedBlock() != null) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (event.getClickedBlock().getType() == Material.FLOWER_POT
                            || event.getClickedBlock().getType().toString().startsWith("POTTED_")) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (event.getClickedBlock().getType() == Material.RESPAWN_ANCHOR) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getAction() == Action.PHYSICAL || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (event.getClickedBlock().getType() == Material.REDSTONE_ORE) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (event.getClickedBlock().getType() == Material.PUMPKIN
                        && event.getAction() == Action.RIGHT_CLICK_BLOCK
                        && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SHEARS) {
                    event.setCancelled(true);
                    return;
                }
                if (BLOCKED_TYPES.contains(event.getClickedBlock().getType())) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (event.getClickedBlock().getType() == Material.TRAPPED_CHEST
                            || event.getClickedBlock().getType() == Material.DISPENSER
                            || event.getClickedBlock().getType() == Material.DROPPER
                            || event.getClickedBlock().getType().toString().toLowerCase().contains("shulker")
                            || event.getClickedBlock().getType() == Material.TRAPPED_CHEST
                            || event.getClickedBlock().getType() == Material.CHEST)
                        event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) event.setCancelled(true);
    }

    /**
     * Prevents mobs from trampling blocks
     */
    @EventHandler
    public void onMobTrample(EntityInteractEvent event) {
        if (event.getEntity() instanceof Player) return;
        event.setCancelled(true);
    }
}
