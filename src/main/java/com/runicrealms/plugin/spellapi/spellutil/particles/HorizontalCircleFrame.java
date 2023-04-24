package com.runicrealms.plugin.spellapi.spellutil.particles;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HorizontalCircleFrame implements ParticleFormat {
    private final boolean semiCircle;
    private final float radius;

    public HorizontalCircleFrame(float radius, boolean semiCircle) {
        this.radius = radius;
        this.semiCircle = semiCircle;
    }

    @Override
    public void playParticle(Player player, Particle particle, Location location, Color... color) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            Location taskLocation = location.clone();
            float start = 0, finish = 360;
            if (semiCircle) {
                float yaw = taskLocation.getYaw();
                start = yaw;
                finish = yaw + 180;
            }
            Vector vector;
            for (double a = start; a <= finish; a++) {
                double theta = Math.toRadians(a);
                // x = r * cos(theta), z = r * sin(theta)
                vector = new Vector(this.radius * Math.cos(theta), 0D, this.radius * Math.sin(theta));
                if (particle == Particle.REDSTONE) {
                    player.getWorld().spawnParticle(particle, taskLocation.add(vector), 1, 0, 0, 0, new Particle.DustOptions(color[0], 1));
                } else if (particle == Particle.BLOCK_CRACK) {
                    player.getWorld().spawnParticle(particle, taskLocation.add(vector), 1, 0, 0, 0, Bukkit.createBlockData(Material.BLUE_ICE));
                } else {
                    player.getWorld().spawnParticle(particle, taskLocation.add(vector), 1, 0, 0, 0, 0);
                }
                taskLocation.subtract(vector);
            }
        });
    }

}
