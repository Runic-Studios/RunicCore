package com.runicrealms.plugin.spellapi.spellutil.particles;

import com.runicrealms.plugin.RunicCore;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
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
    public void playParticle(Player player, Particle particle, Location location, double particleSpacing, Color... color) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            Location taskLocation = location.clone();
            World world = player.getWorld();
            float start = 0, finish = 360;
            if (semiCircle) {
                float yaw = taskLocation.getYaw();
                start = yaw;
                finish = yaw + 180;
            }
            Vector vector;
            BlockData blockData = null;
            if (particle == Particle.BLOCK_CRACK) {
                blockData = Bukkit.createBlockData(Material.BLUE_ICE);
            }
            Particle.DustOptions dustOptions = null;
            if (particle == Particle.REDSTONE) {
                dustOptions = new Particle.DustOptions(color[0], 1);
            }
            for (double a = start; a <= finish; a += particleSpacing) {
                double theta = Math.toRadians(a);
                // x = r * cos(theta), z = r * sin(theta)
                vector = new Vector(this.radius * FastMath.cos(theta), 0D, this.radius * FastMath.sin(theta));
                Location particleLocation = taskLocation.add(vector);
                if (particle == Particle.REDSTONE) {
                    world.spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0, dustOptions);
                } else if (particle == Particle.BLOCK_CRACK) {
                    world.spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0, blockData);
                } else if (particle == Particle.FALLING_DUST) {
                    world.spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0, Material.COAL_BLOCK.createBlockData());
                } else {
                    world.spawnParticle(particle, particleLocation, 1, 0, 0, 0, 0);
                }
                taskLocation.subtract(vector);
            }
        });
    }

    public void playParticle(Player player, Particle particle, Location location, Color... color) {
        playParticle(player, particle, location, 15, color);
    }

}
