package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Hexagon extends BukkitRunnable {
    private final Location location;
    private final double duration;
    private final double radius;
    private final int points = 100; // Number of points per line
    private double angle = 0;
    private double count = 1;

    public Hexagon(Location location, double duration, double radius) {
        this.location = location;
        this.duration = duration;
        this.radius = radius;
    }

    @Override
    public void run() {
        count++;
        if (count > duration) {
            this.cancel();
            return;
        }
        // For each line of the hexagon
        for (int i = 0; i < 6; i++) {

            // Calculate the start and end points of the line
            double angleStart = i * Math.PI / 3;
            double angleEnd = ((i + 1) % 6) * Math.PI / 3;

            Location startPoint = location.clone().add(radius * Math.cos(angleStart), 0, radius * Math.sin(angleStart));
            Location endPoint = location.clone().add(radius * Math.cos(angleEnd), 0, radius * Math.sin(angleEnd));

            // Interpolate between the start and end points
            for (int j = 0; j < points; j++) {
                double t = (double) j / (points - 1);
                Location point = startPoint.clone().add(endPoint.clone().subtract(startPoint).multiply(t));

                // Spawn particle at the calculated location
                location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, point, 1, 0, 0, 0, 0);
            }
        }
    }
}
