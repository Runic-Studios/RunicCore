package com.runicrealms.plugin.tutorial;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class TutorialTask {

    public TutorialTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                explosionEffect();
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 100, 160);
    }

    private void explosionEffect() {

        Location loc = new Location(Bukkit.getWorld("Alterra"), -2320, 65, 1770);
        Random rand = new Random();
        int x = rand.nextInt(10) - 5;
        int y = rand.nextInt(10) - 5;
        int z = rand.nextInt(10) - 5;

        Location newLoc = loc.add(x, y, z);

        if (newLoc.getWorld() == null) return;

        // blow up the fortress!
        newLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, newLoc, 15, 10, 10, 10);
        newLoc.getWorld().spawnParticle(Particle.LAVA, newLoc, 25, 10, 10, 10, 0);
        newLoc.getWorld().playSound(newLoc, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 1.0f);
    }
}
