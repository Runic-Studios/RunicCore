package com.runicrealms.plugin.api;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.InvisibleStandSpawner;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

public class ArmorStandAPI {

    /**
     * Spawns an invisible armor stand using the parameterized consumer.
     *
     * @param location to spawn the armor stand
     * @return the armor stand entity
     */
    public static ArmorStand spawnArmorStand(Location location) {
        InvisibleStandSpawner consumer = new InvisibleStandSpawner();
        if (location.getWorld() == null) {
            RunicCore.getInstance().getLogger().info("An error occurred.");
            return null;
        }
        return location.getWorld().spawn(location, ArmorStand.class, consumer);
    }

    /*
    Returns the corresponding radians (from degrees) for use in the Euler Angle poses
     */
    public static float degreesToRadians(double degrees) {
        return (float) ((degrees * Math.PI) / 180);
    }
}
