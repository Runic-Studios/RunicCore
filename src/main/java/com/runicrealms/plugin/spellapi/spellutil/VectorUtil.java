package com.runicrealms.plugin.spellapi.spellutil;

import org.bukkit.Color;
import org.bukkit.Location;
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


    public static void drawLine(Player pl, Particle particle, Color color,
                                Location point1, Location point2, double space) {
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        for (double length = 0; length < distance; p1.add(vector)) {
            if (particle == Particle.REDSTONE) {
                pl.getWorld().spawnParticle(particle,
                        new Location(pl.getWorld(), p1.getX(), p1.getY(), p1.getZ()),
                        25, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
            } else {
                pl.getWorld().spawnParticle(particle,
                        new Location(pl.getWorld(), p1.getX(), p1.getY(), p1.getZ()),
                        25, 0, 0, 0, 0);
            }
            length += space;
        }
    }
}
