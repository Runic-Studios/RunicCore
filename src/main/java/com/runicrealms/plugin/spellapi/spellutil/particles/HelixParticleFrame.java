package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HelixParticleFrame implements ParticleFormat {

    private double radius;
    private int height;
    private double frequency;

    public HelixParticleFrame(double radius, int height, double frequency) {
        this.radius = radius;
        this.height = height;
        this.frequency = frequency;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public void playParticle(Player player, Particle particle, Location location, double particleSpacing, Color... color) {
        location = location.clone();
        final double totalDegrees = ((360 / this.frequency) * this.height);
        final Vector constantIncrement = new Vector(0D, (this.height / totalDegrees), 0D);

        for (double a = 0; a <= totalDegrees; a += particleSpacing) {
            double theta = Math.toRadians(a % 360);
            Vector vector = new Vector(this.radius * Math.cos(theta), 0D, this.radius * Math.sin(theta));
            if (particle == Particle.REDSTONE) {
                player.getWorld().spawnParticle(particle, location.add(vector).add(constantIncrement),
                        1, 0, 0, 0, 0, new Particle.DustOptions(color[0], 1));
            } else if (particle == Particle.BLOCK_CRACK) {
                BlockData blockData = Bukkit.createBlockData(Material.PACKED_ICE);
                player.getWorld().spawnParticle(particle, location.add(vector).add(constantIncrement), 1, 0, 0, 0, 0, blockData);
            } else {
                player.getWorld().spawnParticle(particle, location.add(vector).add(constantIncrement), 1, 0, 0, 0, 0);
            }
            location.subtract(vector);
        }
    }

    public void playParticle(Player player, Particle particle, Location location, Color... color) {
        playParticle(player, particle, location, 15, color);
    }

}
