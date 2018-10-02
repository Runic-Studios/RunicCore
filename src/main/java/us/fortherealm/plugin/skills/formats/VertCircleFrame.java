package us.fortherealm.plugin.skills.formats;

import org.bukkit.*;
import org.bukkit.util.Vector;

public class VertCircleFrame implements ParticleFormat {

    private float radius;

    public VertCircleFrame(float radius) {
        this.radius = radius;
    }

    @Override
    public void playParticle(Particle particle, Location location) {
        location = location.clone();

        for (double a = 0; a <= 360; a++){
            double theta = Math.toRadians(a);
            Vector vector = new Vector(this.radius * Math.cos(theta), this.radius * Math.sin(theta), 0D);

            location.getWorld().spawnParticle(particle, location.add(vector), 1, 0, 0, 0, 0);
            location.subtract(vector);
        }
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

}
