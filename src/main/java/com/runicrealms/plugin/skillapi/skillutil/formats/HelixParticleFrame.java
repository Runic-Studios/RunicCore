package com.runicrealms.plugin.skillapi.skillutil.formats;

import org.bukkit.*;
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

    @Override
    public void playParticle(Particle particle, Location location) {
        location = location.clone();
        final double totalDegrees = ((360 / this.frequency) * this.height);
        final Vector constantIncrement = new Vector(0D, (this.height / totalDegrees), 0D);

        for (double a = 0; a <= totalDegrees; a++) {
            double theta = Math.toRadians(a % 360);
            Vector vector = new Vector(this.radius * Math.cos(theta), 0D, this.radius * Math.sin(theta));

            location.getWorld().spawnParticle(particle, location.add(vector).add(constantIncrement), 1, 0, 0, 0, 0);
            location.subtract(vector);
        }
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

}
