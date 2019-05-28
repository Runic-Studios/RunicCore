package com.runicrealms.plugin.healthbars;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

import java.util.Objects;

/**
 * The MobHealthManager cleans up any armor stands which are labeled as healthbars and don't have passngers.
 * Basically, it removes trash from the server.
 */
public class MobHealthManager {

    public MobHealthManager() {
        cleanupTask();
        fullCleanTask();
        fixHealthBars();
    }

    /**
     * Removes stray armor stands every 30 seconds async.
     */
    private void fullCleanTask() {

        new BukkitRunnable() {
            @Override
            public void run() {
                fullClean();
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

                    for (Entity en : Objects.requireNonNull(Bukkit.getWorld(world)).getEntities()) {
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

            for (Entity en : Objects.requireNonNull(Bukkit.getWorld(world)).getEntities()) {

                // remove stray armorstands
                if (en instanceof ArmorStand && !en.hasMetadata("healthbar")) {
                    en.remove();
                }
            }
        }
    }

    private void fixHealthBars() {

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < Bukkit.getWorlds().size(); i++) {

                    String world = Bukkit.getWorlds().get(i).getName();

                    for (Entity en : Objects.requireNonNull(Bukkit.getWorld(world)).getEntities()) {

                        if (en instanceof Player) continue;

                        if (en instanceof ArmorStand) continue;

                        if (en.hasMetadata("NPC")) continue;

                        if (!(en instanceof LivingEntity)) continue;

                        LivingEntity le = (LivingEntity) en;

                        if (en.getPassengers().size() == 0) {
                            MobHealthBars.setupMob(le);
                        }
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 100, 100L);
    }
}
