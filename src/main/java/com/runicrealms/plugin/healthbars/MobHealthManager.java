package com.runicrealms.plugin.healthbars;

import com.runicrealms.plugin.RunicCore;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

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
                        // remove stuck horses
                        if (en instanceof Horse && en.getPassengers().size() == 0 && !en.hasMetadata("NPC")) {
                            en.remove();
                        } else if (en instanceof Horse) {
                            Horse horse = (Horse) en;
                            if (horse.getColor() == Horse.Color.CREAMY) {
                                horse.getWorld().spawnParticle(Particle.FLAME, horse.getEyeLocation(), 15, 0.6f, 0.5f, 0.6f, 0);
                            } else if (horse.getColor() == Horse.Color.DARK_BROWN) {
                                horse.getWorld().spawnParticle(Particle.BLOCK_DUST, horse.getEyeLocation(),
                                        5, 0.6F, 0.5F, 0.6F, 0, Material.PACKED_ICE.createBlockData());
                            }
                        }
                        if (en.hasMetadata("healthbar")
                                && en.getVehicle() == null) {
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
                if (en instanceof ArmorStand
                        && !en.hasMetadata("healthbar")
                        && !en.hasMetadata("indicator")
                        && !MythicMobs.inst().getAPIHelper().isMythicMob(en)) {
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

                        if (en instanceof Horse) continue;

                        LivingEntity le = (LivingEntity) en;

                        if (en.getPassengers().size() == 0) {
                            MobHealthBars.setupEntityHealthbar(le);
                        }
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 100, 100L);
    }
}
