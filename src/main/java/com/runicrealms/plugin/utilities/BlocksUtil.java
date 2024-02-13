package com.runicrealms.plugin.utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;

public final class BlocksUtil {

    /**
     * Finds the nearest block of a valid type to the given location (within the given radius)
     *
     * @param location       to center around
     * @param radius         of blocks to check
     * @param validMaterials a set of Materials that are considered valid targets
     * @return the nearest valid block to location (or null if none could be found)
     */
    public static Location findNearestValidBlock(Location location, int radius, Set<Material> validMaterials) {
        Location bestLocation = null;
        double minDistanceSquared = Double.MAX_VALUE;

        for (int y = -radius; y <= radius; y++) { // Search vertically around the location, including below
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block currentBlock = location.clone().add(x, y, z).getBlock();
                    if (validMaterials.contains(currentBlock.getType())) {
                        double distanceSquared = location.distanceSquared(currentBlock.getLocation());
                        if (distanceSquared < minDistanceSquared) {
                            minDistanceSquared = distanceSquared;
                            bestLocation = currentBlock.getLocation();
                        }
                    }
                }
            }
        }

        return bestLocation;
    }
}
