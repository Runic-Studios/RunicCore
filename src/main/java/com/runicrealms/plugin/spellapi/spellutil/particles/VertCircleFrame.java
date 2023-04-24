package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VertCircleFrame implements ParticleFormat {

    private float radius;

    public VertCircleFrame(float radius) {
        this.radius = radius;
    }

    @Override
    public void playParticle(Player player, Particle particle, Location location, Color... color) {
        location = location.clone();

        for (double a = 0; a <= 360; a++) {
            double theta = Math.toRadians(a);
            Vector vector = new Vector(this.radius * Math.cos(theta), this.radius * Math.sin(theta), location.getDirection().getZ());
            player.getWorld().spawnParticle(particle, location.add(vector), 1, 0, 0, 0, 0);
            location.subtract(vector);
        }
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

}
