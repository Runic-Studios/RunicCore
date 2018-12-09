package us.fortherealm.plugin.skillapi.skillutil.formats;

import org.bukkit.Location;
import org.bukkit.Particle;

@FunctionalInterface
public interface ParticleFormat {

    void playParticle(Particle particle, Location location);
}
