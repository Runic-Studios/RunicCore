package com.runicrealms.plugin.spellapi.spellutil.particles;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EntityTrail {

    public static BukkitTask entityTrail(Entity entity, Particle particle) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (entity.isDead())
                    this.cancel();
                entity.getWorld().spawnParticle(particle, entity.getLocation(), 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1);
    }

    public static BukkitTask entityTrail(Entity entity, Color color) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (entity.isDead())
                    this.cancel();
                entity.getWorld().spawnParticle(Particle.REDSTONE, entity.getLocation(), 1, 0, 0, 0,
                        new Particle.DustOptions(color, 1));
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1);
    }
}
