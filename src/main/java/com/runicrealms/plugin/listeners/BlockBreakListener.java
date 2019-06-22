package com.runicrealms.plugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

/**
 * Prevents players from breaking blocks on the server, but doesn't listen for gathering materials, since those
 * are handled separately.
 */
public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Material type = e.getBlock().getType();
        if (e.getBlock().getType() == Material.AIR) return;
        switch (type) {

            // farming materials
            case WHEAT:
                return;

            // mining materials
            case DIAMOND_ORE:
                return;
            case EMERALD_ORE:
                return;
            case GOLD_ORE:
                return;
            case IRON_ORE:
                return;
            case LAPIS_ORE:
                return;
            case REDSTONE_ORE:
                return;
            case NETHER_QUARTZ_ORE:
                return;

            // woodcutting materials
            case OAK_WOOD:
                return;
            case SPRUCE_WOOD:
                return;
            case BIRCH_WOOD:
                return;
            case JUNGLE_WOOD:
                return;
            case ACACIA_WOOD:
                return;
            case DARK_OAK_WOOD:
                return;
            default:
                e.setCancelled(false);
                break;
        }

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevent players from ever placing blocks
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevent item frame destruction
     */
    @EventHandler
    public void onItemFrameBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() == null) return;
        if (!(e.getRemover() instanceof Player)) e.setCancelled(true);
        if (e.getEntity() instanceof ItemFrame
                && e.getRemover() instanceof Player
                && !e.getRemover().isOp()) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevents item frame item removal
     */
    @EventHandler
    public void itemFrameItemRemoval(EntityDamageEvent e) {
        if (e.getEntity() instanceof ItemFrame) {
            e.setCancelled(true);
        }
    }
}
