package com.runicrealms.plugin.spellapi.spellutil.particles;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Bubble {

    public static void bubbleEffect(Location location, Particle particle, int piCoefficient, int delay, int period, double radius){
        new BukkitRunnable(){
            double phi = 0;
            public void run(){
                phi += Math.PI/10;
                for (double theta = 0; theta <= 2*Math.PI; theta += Math.PI/40) {
                    double x = radius*cos(theta)*sin(phi);
                    double y = radius*cos(phi) + 1.5;
                    double z = radius*sin(theta)*sin(phi);
                    location.add(x,y,z);
                    location.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);
                    location.subtract(x,y,z);
                }
                if(phi > (piCoefficient * Math.PI)){ // # of oscillations
                    this.cancel();
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), delay, period);
    }
}
