package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface ParticleFormat {

    /**
     * @param player   who spawned the particle
     * @param particle the particle to display
     * @param location to spawn the particle
     * @param color    optional argument for redstone color (if the particle is redstone)
     */
    void playParticle(Player player, Particle particle, Location location, Color... color);
}
