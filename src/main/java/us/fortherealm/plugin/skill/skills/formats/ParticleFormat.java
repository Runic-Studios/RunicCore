package us.fortherealm.plugin.skill.skills.formats;

import org.bukkit.Location;
import org.bukkit.Particle;

@FunctionalInterface
public interface ParticleFormat {

    void playParticle(Particle particle, Location location);
}
