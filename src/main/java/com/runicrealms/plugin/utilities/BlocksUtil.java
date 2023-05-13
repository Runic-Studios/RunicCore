package com.runicrealms.plugin.utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class BlocksUtil {

    /**
     * Finds the nearest air block to the given location (within the given radius)
     *
     * @param location to center around
     * @param radius   of blocks to check
     * @return the nearest air block to location (or null if none could be found)
     */
    public static Location findNearestAir(Location location, int radius) {
        Location bestLocation = null;
        double minDistanceSquared = Double.MAX_VALUE;

        for (int y = 0; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    Block currentBlock = location.clone().add(x, y, z).getBlock();
                    if (currentBlock.getType() == Material.AIR) {
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
