package us.fortherealm.plugin.healthbars;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;

/**
 * The MobHealthManager cleans up any armor stands which are labeled as healthbars and don't have passngers.
 * Basically, it removes trash from the server.
 */
public class MobHealthManager {

    public MobHealthManager() {
        cleanupTask();
    }

    private void cleanupTask() {

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < Bukkit.getWorlds().size(); i++) {

                    String world = Bukkit.getWorlds().get(i).getName();

                    try {
                        for (Entity en : Bukkit.getWorld(world).getEntities()) {
                            if (en.hasMetadata("healthbar") && en.getVehicle() == null) {
                                en.remove();
                            }
                        }
                    } catch (NullPointerException e) {
                        Bukkit.broadcastMessage("no entities");
                    }
                }
            }
        }.runTaskTimerAsynchronously(FTRCore.getInstance(), 60, 20);
    }

    public void fullClean() {

       // new BukkitRunnable() {
           // @Override
            //public void run() {
                for (int i = 0; i < Bukkit.getWorlds().size(); i++) {

                    String world = Bukkit.getWorlds().get(i).getName();

                    for (Entity en : Bukkit.getWorld(world).getEntities()) {
                        if (en instanceof ArmorStand) {
                            en.remove();
                        }
                    }

                }
            }
       // }.runTaskLaterAsynchronously(FTRCore.getInstance(), 100);
   // }
}
