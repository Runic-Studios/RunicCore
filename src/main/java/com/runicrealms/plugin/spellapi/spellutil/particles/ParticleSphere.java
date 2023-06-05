package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleSphere extends BukkitRunnable {
    private final Location center;
    private final double radius;
    private final Particle particleType;
    private final int particleCount;
    private final long endTime;

    public ParticleSphere(Location center, double radius, Particle particleType, int particleCount, int duration) {
        this.center = center;
        this.radius = radius;
        this.particleType = particleType;
        this.particleCount = particleCount;
        this.endTime = System.currentTimeMillis() + (duration * 1000L);
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() >= endTime) {
            this.cancel();
            return;
        }

        for (int i = 0; i < particleCount; i++) {
            double theta = 2.0 * Math.PI * Math.random();
            double phi = Math.acos(2.0 * Math.random() - 1.0);
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);

            Location particleLocation = center.clone().add(x, y, z);

            center.getWorld().spawnParticle(particleType, particleLocation, 1, 0, 0, 0, 0);
        }
    }
}