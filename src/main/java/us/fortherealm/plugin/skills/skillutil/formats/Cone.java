package us.fortherealm.plugin.skills.skillutil.formats;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Cone {

    public static void coneEffect(final Player player, Particle particle, int piCoefficient, int delay, int period){
        new BukkitRunnable(){
            double phi = 0;
            public void run(){
                phi = phi + Math.PI/8;
                double x, y, z;

                Location playerLoc = player.getLocation();
                for (double t = 0; t <= 2*Math.PI; t = t + Math.PI/16){
                    for (double i = 0; i <= 1; i = i + 1){
                        x = 0.4*(2*Math.PI-t)*0.5*cos(t + phi + i*Math.PI);
                        y = 0.5*t;
                        z = 0.4*(2*Math.PI-t)*0.5*sin(t + phi + i*Math.PI);
                        playerLoc.add(x, y, z);
                        if (particle ==  Particle.REDSTONE) {
                            player.getWorld().spawnParticle(Particle.REDSTONE, playerLoc,
                                    1, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 1));
                        } else {
                            playerLoc.getWorld().spawnParticle(particle, playerLoc, 1, 0, 0, 0, 0);
                        }
                        playerLoc.subtract(x,y,z);
                    }

                }

                if(phi > piCoefficient * Math.PI){ // length of particle effect
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), delay, period);
    }
}
