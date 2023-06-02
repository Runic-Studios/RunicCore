package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class RotatingParticleEffect {
    private final Player player;
    private final Particle particle;
    private final double radius;
    private final double step;
    private double t;
    private Color color;

    public RotatingParticleEffect(Player player, Particle particle, double radius, double step, double t, Color... color) {
        this.player = player;
        this.particle = particle;
        this.radius = radius;
        this.step = step;
        this.t = t;
        if (color.length > 0) {
            this.color = color[0];
        }
    }

    public void show() {
        final Location location = player.getLocation().clone();
        t = t + (Math.PI / 16) * step;
        double x = radius * Math.sin(t);
        double z = radius * Math.cos(t);
        location.add(x, 1, z);
        if (color != null) {
            player.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
        } else {
            player.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);
        }
    }

    public double getT() {
        return t;
    }
}

