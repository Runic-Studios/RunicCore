package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HelixReverseParticleFrame implements ParticleFormat {

    private double endRadius;
    private int height;
    private double frequency;

    public HelixReverseParticleFrame(double endRadius, int height, double frequency) {
        this.endRadius = endRadius;
        this.height = height;
        this.frequency = frequency;
    }

    public double getEndRadius() {
        return endRadius;
    }

    public void setEndRadius(double endRadius) {
        this.endRadius = endRadius;
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

    @Override
    public void playParticle(Player player, Particle particle, Location location, double particleSpacing, Color... color) {
        location = location.clone();
        final double totalDegrees = ((360 / this.frequency) * this.height);
        final Vector constantIncrement = new Vector(0D, (this.height / totalDegrees), 0D);
        for (double a = 0; a <= totalDegrees; a += particleSpacing) {
            double theta = Math.toRadians(a % 360);
            double stageRadius = ((this.endRadius / this.height) * (a / totalDegrees));
            Vector vector = new Vector(stageRadius * Math.cos(theta), 0D, stageRadius * Math.sin(theta));

            player.getWorld().spawnParticle(particle, location.add(vector).add(constantIncrement), 1, 0, 0, 0, 0);
            location.subtract(vector);
        }
    }

    public void playParticle(Player player, Particle particle, Location location, Color... color) {
        playParticle(player, particle, location, 15, color);
    }

}
