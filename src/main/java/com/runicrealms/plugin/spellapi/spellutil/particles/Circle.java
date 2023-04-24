package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Circle {

    /**
     * Creates a ring of particles around the given location, spawned in the player's world, with the given radius
     * IMPORTANT: this is costly. try to create particle async if possible
     *
     * @param player       who summoned the particles
     * @param castLocation around which to build the ring
     * @param radius       of the circle
     * @param particle     to use for the circle
     */
    public static void createParticleCircle(Player player, Location castLocation, int radius, Particle particle) {
        final Location location = castLocation.clone();
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * (float) radius;
            z = Math.sin(angle) * (float) radius;
            location.add(x, 0, z);
            player.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);
            location.subtract(x, 0, z);
        }
    }

    /**
     * Creates a ring of particles around the given location, spawned in the player's world, with the given radius
     * IMPORTANT: this is costly. try to create particle async if possible
     *
     * @param player       who summoned the particles
     * @param castLocation around which to build the ring
     * @param radius       of the circle
     * @param particle     to use
     * @param color        of the redstone, for use with redstone particles
     */
    public static void createParticleCircle(Player player, Location castLocation, int radius, Particle particle, Color color) {
        final Location location = castLocation.clone();
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * (float) radius;
            z = Math.sin(angle) * (float) radius;
            location.add(x, 0, z);
            player.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
            location.subtract(x, 0, z);
        }
    }
}
