package us.fortherealm.plugin.skillapi.skilltypes.skillutil;

import us.fortherealm.plugin.Main;
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
                Location loc = location;
                for (double theta = 0; theta <= 2*Math.PI; theta += Math.PI/40) {
                    double x = radius*cos(theta)*sin(phi);
                    double y = radius*cos(phi) + 1.5;
                    double z = radius*sin(theta)*sin(phi);
                    loc.add(x,y,z);
                    loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x,y,z);
                }
                if(phi > (piCoefficient * Math.PI)){ // # of oscillations
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), delay, period);
    }
}
