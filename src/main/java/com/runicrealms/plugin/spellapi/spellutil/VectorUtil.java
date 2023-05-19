package com.runicrealms.plugin.spellapi.spellutil;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VectorUtil {

    public static Vector rotateVectorAroundY(Vector vector, double degrees) {
        Vector newVector = vector.clone();
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sine = Math.sin(rad);
        double x = vector.getX();
        double z = vector.getZ();
        newVector.setX(cos * x - sine * z);
        newVector.setZ(sine * x + cos * z);
        return newVector;
    }

    /**
     * @param player
     * @param particle
     * @param color
     * @param point1
     * @param point2
     * @param space
     * @return
     */
    public static Vector drawLine(Player player, Particle particle, Color color, Location point1, Location point2, double space, int count) {
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        for (double length = 0; length < distance; p1.add(vector)) {
            if (particle == Particle.REDSTONE) {
                player.getWorld().spawnParticle(particle,
                        new Location(player.getWorld(), p1.getX(), p1.getY(), p1.getZ()),
                        count, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
            } else {
                player.getWorld().spawnParticle(particle,
                        new Location(player.getWorld(), p1.getX(), p1.getY(), p1.getZ()),
                        count, 0, 0, 0, 0);
            }
            length += space;
        }
        return vector;
    }

    public static Vector drawLine(Player player, Material material, Location point1,
                                  Location point2, double space, int count, float offset) {
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        for (double length = 0; length < distance; p1.add(vector)) {
            player.getWorld().spawnParticle(Particle.BLOCK_CRACK,
                    new Location(player.getWorld(), p1.getX(), p1.getY(), p1.getZ()),
                    count, 0, offset, 0, 0, Bukkit.createBlockData(material));
            length += space;
        }
        return vector;
    }

    public static Vector drawLine(Player player, Particle particle, Color color, Location point1, Location point2, double space, int count, float offset) {
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        for (double length = 0; length < distance; p1.add(vector)) {
            if (particle == Particle.REDSTONE) {
                player.getWorld().spawnParticle(particle,
                        new Location(player.getWorld(), p1.getX(), p1.getY(), p1.getZ()),
                        count, 0, offset, 0, 0, new Particle.DustOptions(color, 1));
            } else {
                player.getWorld().spawnParticle(particle,
                        new Location(player.getWorld(), p1.getX(), p1.getY(), p1.getZ()),
                        count, 0, offset, 0, 0);
            }
            length += space;
        }
        return vector;
    }
}
