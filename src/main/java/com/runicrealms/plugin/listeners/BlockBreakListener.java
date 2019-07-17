package com.runicrealms.plugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
     * Prevent item frame, painting destruction
     */
    @EventHandler
    public void onItemFrameBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() == null) return;
        if (!(e.getRemover() instanceof Player)) e.setCancelled(true);
        if ((e.getEntity() instanceof ItemFrame || e.getEntity() instanceof Painting)
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
        if (e.getEntity() instanceof ItemFrame && e.getEntity() instanceof Painting) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevents players from breaking fires
     */
    @EventHandler
    public void onFireBreak(PlayerInteractEvent e) {

        if (e.getPlayer().isOp()) return;
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        Block target = e.getClickedBlock();

        Block[] adjacent = {
                target.getRelative(BlockFace.NORTH),
                target.getRelative(BlockFace.SOUTH),
                target.getRelative(BlockFace.WEST),
                target.getRelative(BlockFace.EAST),
                target.getRelative(BlockFace.UP),
                target.getRelative(BlockFace.DOWN)};

        for (Block source : adjacent) {
            if (source.getType() == Material.FIRE) {
                e.setCancelled(true);
            }
        }
    }
}
