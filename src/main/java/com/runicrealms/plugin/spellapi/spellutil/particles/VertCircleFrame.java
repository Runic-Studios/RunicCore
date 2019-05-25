package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.*;
import org.bukkit.util.Vector;

public class VertCircleFrame implements ParticleFormat {

    private float radius;

    public VertCircleFrame(float radius) {
        this.radius = radius;
    }

    @Override
    public void playParticle(Particle particle, Location location, Color color) {
        location = location.clone();

        for (double a = 0; a <= 360; a++){
            double theta = Math.toRadians(a);
            Vector vector = new Vector(this.radius * Math.cos(theta), this.radius * Math.sin(theta), location.getDirection().getZ());

            location.getWorld().spawnParticle(particle, location.add(vector), 1, 0, 0, 0, 0);
            location.subtract(vector);
        }
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

}
