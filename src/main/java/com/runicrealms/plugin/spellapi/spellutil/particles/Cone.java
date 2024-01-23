package com.runicrealms.plugin.spellapi.spellutil.particles;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Cone {

    /**
     * Creates a helix cone around given livingEntity.
     *
     * @param livingEntity livingEntity to create particle around
     * @param particle     type of particle to use
     * @param DURATION     length of effect (in seconds)
     * @param delay        delay of effect (in ticks)
     * @param period       of effect (in ticks)
     * @param color        color of effect if using redstone
     * @return return BukkitTask that can be cancelled
     */
    public static BukkitTask coneEffect(final LivingEntity livingEntity, Particle particle, double DURATION, int delay, long period, Color color) {

        BukkitTask bukkitTask = new BukkitRunnable() {

            double phi = 0;

            public void run() {

                if (livingEntity.isDead()) {
                    this.cancel();
                } else {

                    phi = phi + Math.PI / 8;
                    double x, y, z;

                    Location location = livingEntity.getLocation();
                    for (double t = 0; t <= 2 * Math.PI; t = t + Math.PI / 16) {
                        for (double i = 0; i <= 1; i = i + 1) {
                            x = 0.4 * (2 * Math.PI - t) * 0.5 * cos(t + phi + i * Math.PI);
                            y = 0.5 * t;
                            z = 0.4 * (2 * Math.PI - t) * 0.5 * sin(t + phi + i * Math.PI);
                            location.add(x, y, z);
                            if (particle == Particle.REDSTONE) {
                                livingEntity.getWorld().spawnParticle(Particle.REDSTONE, location,
                                        1, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
                            } else if (particle == Particle.NOTE) {
                                livingEntity.getWorld().spawnParticle(Particle.NOTE, location, 0, 1d, 0.0d, 0.0d, 0.1d);
                            } else if (particle == Particle.BLOCK_CRACK) {
                                BlockData blockData = Bukkit.createBlockData(Material.BLUE_ICE);
                                livingEntity.getWorld().spawnParticle(Particle.BLOCK_CRACK, location, 1, 0, 0, 0, 0.1d, blockData);

                            } else {
                                livingEntity.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);
                            }
                            location.subtract(x, y, z);
                        }

                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), delay, period);

        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), bukkitTask::cancel, (long) DURATION * 20L);

        return bukkitTask;
    }
}
