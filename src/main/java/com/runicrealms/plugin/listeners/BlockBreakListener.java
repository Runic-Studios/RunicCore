package com.runicrealms.plugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Prevents players from breaking blocks on the server, but doesn't listen for gathering materials, since those
 * are handled separately.
 */
public class BlockBreakListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (event.getClickedBlock().getType() == Material.FLOWER_POT
                            || event.getClickedBlock().getType().toString().startsWith("POTTED_")) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (event.getClickedBlock().getType() == Material.RESPAWN_ANCHOR) { event.setCancelled(true); return; }
                if (event.getAction() == Action.PHYSICAL || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (event.getClickedBlock().getType() == Material.REDSTONE_ORE) { event.setCancelled(true); return; }
                }
                if (event.getClickedBlock().getType() == Material.PUMPKIN && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SHEARS) { event.setCancelled(true); return; }
                if (event.getClickedBlock().getType() == Material.BELL) { event.setCancelled(true); return; }
                if (event.getClickedBlock().getType() == Material.JUKEBOX) { event.setCancelled(true); return; }
                if (event.getClickedBlock().getType() == Material.NOTE_BLOCK) { event.setCancelled(true); return; }
                if (event.getClickedBlock().getType() == Material.LODESTONE) { event.setCancelled(true); return; }
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (event.getClickedBlock().getType() == Material.TRAPPED_CHEST
                            || event.getClickedBlock().getType() == Material.DISPENSER
                            || event.getClickedBlock().getType() == Material.DROPPER
                            || event.getClickedBlock().getType().toString().toLowerCase().contains("shulker")
                            || event.getClickedBlock().getType() == Material.TRAPPED_CHEST) event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) event.setCancelled(true);
    }

    @EventHandler
    public void onEnterBed(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }



    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }


    /**
     * Prevents players from breaking blocks but runs last to allow professions to do their thing
     */
    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (e.getBlock().getType() == Material.AIR) return;
        e.setCancelled(true);
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
     * Prevents item frame, painting item removal
     */
    @EventHandler
    public void itemFrameItemRemoval(EntityDamageEvent e) {
        if (e.getEntity() instanceof ItemFrame || e.getEntity() instanceof Painting) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevents players from breaking fires, switching trapdoors, prevents fire charge use
     */
    @EventHandler
    public void onFireBreak(PlayerInteractEvent e) {

        if (e.getClickedBlock() == null) return;
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        Block target = e.getClickedBlock();
        if (e.getPlayer().isOp()) return;

        if (e.getHand() != EquipmentSlot.HAND) return;

        // offhand items
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK
                && e.getMaterial() == Material.FIRE_CHARGE) {
            e.setCancelled(true);
        }


        // disable trapdoor switching
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (target.getType() == Material.OAK_TRAPDOOR
                    || target.getType() == Material.SPRUCE_TRAPDOOR
                    || target.getType() == Material.BIRCH_TRAPDOOR
                    || target.getType() == Material.JUNGLE_TRAPDOOR
                    || target.getType() == Material.ACACIA_TRAPDOOR
                    || target.getType() == Material.DARK_OAK_TRAPDOOR) {
                e.setCancelled(true);
            }
        }

        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;

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
