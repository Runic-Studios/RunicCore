package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ParticleSphere {
    private final Player player;
    private final Sound sound;
    private final double radius;
    private final Particle particleType;
    private final int particleCount;
    private Color color;

    public ParticleSphere(Player player, Sound sound, double radius, Color color, int particleCount) {
        this.player = player;
        this.sound = sound;
        this.radius = radius;
        this.particleType = Particle.REDSTONE;
        this.color = color;
        this.particleCount = particleCount;
    }

    public ParticleSphere(Player player, Sound sound, double radius, Particle particleType, int particleCount) {
        this.player = player;
        this.sound = sound;
        this.radius = radius;
        this.particleType = particleType;
        this.particleCount = particleCount;
    }
    
    public void show() {
        for (int i = 0; i < particleCount; i++) {
            double theta = 2.0 * Math.PI * Math.random();
            double phi = Math.acos(2.0 * Math.random() - 1.0);
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);

            Location particleLocation = player.getLocation().clone().add(x, y, z);
            if (this.color != null) {
                player.getWorld().spawnParticle(particleType, particleLocation, 1, 0, 0, 0, 0, new Particle.DustOptions(color, 1));
            } else {
                player.getWorld().spawnParticle(particleType, particleLocation, 1, 0, 0, 0, 0);
            }
        }
        player.getWorld().playSound(player.getLocation(), sound, 0.5f, 0.25f);
    }
}