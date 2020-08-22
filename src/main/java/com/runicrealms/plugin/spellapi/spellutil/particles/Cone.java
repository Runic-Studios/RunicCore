package com.runicrealms.plugin.spellapi.spellutil.particles;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Cone {

    public static void coneEffect(final LivingEntity player, Particle particle, int DURATION, int delay, long period, Color color){

        new BukkitRunnable(){

            int count = 1;
            double phi = 0;
            public void run(){

                if (count > DURATION || player.isDead()) {
                    this.cancel();
                } else {

                    count += 1;
                    phi = phi + Math.PI / 8;
                    double x, y, z;

                    Location playerLoc = player.getLocation();
                    for (double t = 0; t <= 2 * Math.PI; t = t + Math.PI / 16) {
                        for (double i = 0; i <= 1; i = i + 1) {
                            x = 0.4 * (2 * Math.PI - t) * 0.5 * cos(t + phi + i * Math.PI);
                            y = 0.5 * t;
                            z = 0.4 * (2 * Math.PI - t) * 0.5 * sin(t + phi + i * Math.PI);
                            playerLoc.add(x, y, z);
                            if (particle == Particle.REDSTONE) {
                                player.getWorld().spawnParticle(Particle.REDSTONE, playerLoc,
                                        1, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
                            } else if (particle == Particle.NOTE) {
                                playerLoc.getWorld().spawnParticle(Particle.NOTE, playerLoc, 0, 1d, 0.0d, 0.0d, 0.1d);
                            } else {
                                playerLoc.getWorld().spawnParticle(particle, playerLoc, 1, 0, 0, 0, 0);
                            }
                            playerLoc.subtract(x, y, z);
                        }

                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), delay, period);
    }
}
