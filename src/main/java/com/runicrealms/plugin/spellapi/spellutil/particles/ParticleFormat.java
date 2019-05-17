package com.runicrealms.plugin.spellapi.spellutil.particles;

import org.bukkit.Location;
import org.bukkit.Particle;

@FunctionalInterface
public interface ParticleFormat {

    void playParticle(Particle particle, Location location);
}
