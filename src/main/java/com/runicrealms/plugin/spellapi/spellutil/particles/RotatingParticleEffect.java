package com.runicrealms.plugin.spellapi.spellutil.particles;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RotatingParticleEffect {
    private final Player player;
    private final Particle particle;
    private final double radius;
    private final long duration;
    private final double step;

    /**
     * Creates a particle that rotates around the given player
     *
     * @param duration in seconds
     */
    public RotatingParticleEffect(Player player, Particle particle, double radius, long duration, double step) {
        this.player = player;
        this.particle = particle;
        this.radius = radius;
        this.duration = duration;
        this.step = step;
    }

    public void start() {
        new BukkitRunnable() {

            final Location location = player.getLocation();
            double t = 0;

            @Override
            public void run() {
                t = t + (Math.PI / 16) * step;
                double x = radius * Math.sin(t);
                double z = radius * Math.cos(t);
                location.add(x, 1, z);

                player.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);

                location.subtract(x, 1, z); // Resetting the location for the next calculation

//                if (t > Math.PI * 4 * duration) { // This will make the circle to be drawn for given duration in seconds
//                    this.cancel();
//                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1); // Runs every tick
    }

//    public void start() {
//        new BukkitRunnable() {
//            double t = 0;
//
//            public void run() {
//                Location loc = player.getLocation();
//                Vector direction = loc.getDirection().normalize();
//                double x = radius * Math.cos(t);
//                double z = radius * Math.sin(t);
//                loc.add(direction.crossProduct(new Vector(0, 1, 0)).multiply(new Vector(x, 0, z)));
//
//                player.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
//
//                loc.subtract(direction.crossProduct(new Vector(0, 1, 0)).multiply(new Vector(x, 0, z)));
//                t += Math.PI / 16 * step;
//
//                if (t > Math.PI * 4 * duration) {
//                    this.cancel();
//                }
//            }
//        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
//    }
}
