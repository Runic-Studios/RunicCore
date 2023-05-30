package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Hexagon extends BukkitRunnable {
    private final Location location;
    private final Sound sound;
    private final float pitch;
    private final double duration;
    private final double radius;
    private final Particle particle;
    private final int points = 50; // Number of points per line (particle density)
    private Color color = null;
    private Material material = Material.AIR;
    private double count = 1;

    /**
     * Used for block crack particle
     *
     * @param material of the block crack particle
     */
    public Hexagon(Location location, Sound sound, float pitch, double duration, double radius, Material material) {
        this.location = location;
        this.sound = sound;
        this.pitch = pitch;
        this.duration = duration;
        this.radius = radius;
        this.particle = Particle.BLOCK_CRACK;
        this.material = material;
    }

    public Hexagon(Location location, Sound sound, float pitch, double duration, double radius, Particle particle, Color... color) {
        this.location = location;
        this.sound = sound;
        this.pitch = pitch;
        this.duration = duration;
        this.radius = radius;
        this.particle = particle;
        this.color = color[0];
    }

    @Override
    public void run() {
        if (count > duration) {
            this.cancel();
            return;
        }

        count++;

        assert location.getWorld() != null;
        location.getWorld().playSound(location, sound, 0.5f, pitch);
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
                if (particle == Particle.REDSTONE) {
                    location.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
                } else if (particle == Particle.BLOCK_CRACK) {
                    location.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0, Bukkit.createBlockData(material));
                } else {
                    location.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
                }
            }
        }
    }
}
