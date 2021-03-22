package com.runicrealms.plugin.spellapi.spellutil;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;

public class EffectUtil {

    private static final BlockFace[] cage = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

    /**
     * Helpful method to create a 'trap' of blocks around given entity.
     * @param location location of the entity to center trap around
     * @param material material of the trap, which block?
     * @param duration duration of the trap (seconds)
     */
    public static void trapEntity(Location location, Material material, int duration) {
        Map<Block, BlockData> changedBlocks = new HashMap<>();
        Location[] locs = new Location[]{location, location.clone().add(0,1,0)};
        for (Location loc : locs) {
            for (BlockFace bf : cage) {
                Block block = loc.getBlock().getRelative(bf, 1);
                changedBlocks.put(block, block.getBlockData());
                block.setType(material);
            }
        }
        // also block above the player
        Block top = location.clone().add(0,2,0).getBlock();
        changedBlocks.put(top, top.getBlockData());
        top.setType(material);
        // block below
        Block bottom = location.clone().add(0,-1,0).getBlock();
        changedBlocks.put(bottom, bottom.getBlockData());
        bottom.setType(material);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            for (Block block : changedBlocks.keySet())
                block.setType(changedBlocks.get(block).getMaterial());
        }, duration * 20L);
    }
}
