package com.runicrealms.plugin.healthbars;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

/**
 * The MobHealthManager cleans up any armor stands which are labeled as healthbars and don't have passngers.
 * Basically, it removes trash from the server.
 */
public class MobHealthManager {

    public MobHealthManager() {
        cleanupTask();
        fullCleanTask();
    }

    /**
     * Removes stray armor stands every 30 seconds async.
     */
    private void fullCleanTask() {

        new BukkitRunnable() {
            @Override
            public void run() {

            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 100, 600);
    }

    /**
     * Removes armor stands designated as 'healthbar' every second async.
     */
    private void cleanupTask() {

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < Bukkit.getWorlds().size(); i++) {

                    String world = Bukkit.getWorlds().get(i).getName();

                    for (Entity en : Bukkit.getWorld(world).getEntities()) {
                        if (en.hasMetadata("healthbar") && en.getVehicle() == null) {
                            en.remove();
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 100, 20);
    }

    public void fullClean() {

        for (int i = 0; i < Bukkit.getWorlds().size(); i++) {

            String world = Bukkit.getWorlds().get(i).getName();

            for (Entity en : Bukkit.getWorld(world).getEntities()) {
                if (en instanceof ArmorStand) {
                    en.remove();
                }
            }
        }
    }
}
