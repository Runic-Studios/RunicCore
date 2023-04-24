package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * More of a handler. Controls the custom day night cycle on the server. Uses custom speeds for day/night
 */
public class DaylightCycleListener {
    private static final int DAY_THRESHOLD = 12000;
    private static final int TICKS_PER_CYCLE = 2;
    /*
    Slows down day from 10min (default) to 20min.
    Task runs every 2 ticks, so 10 times per vanilla tick cycle (20 ticks)
    In 20 ticks, day will progress 10(2 * 0.5) = 10 ticks
    Night will progress 10(2 * 1.0) = 20 ticks (default speed, or 7min)
     */
    private static final double DAY_MULTIPLIER = 0.5D; // Half speed
    private static final double NIGHT_MULTIPLIER = 1.0D; // Normal speed

    public DaylightCycleListener() {
        // Reset time to day on server startup
        World world = Bukkit.getWorld("Alterra");
        assert world != null;
        world.setTime(0);
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), this::updateDaylightCycle, 0, TICKS_PER_CYCLE);
    }

    private void updateDaylightCycle() {
        World world = Bukkit.getWorld("Alterra");
        assert world != null;
        long time = world.getTime();
        if (time <= DAY_THRESHOLD) { // Slow down daytime
            world.setTime(time + (long) (TICKS_PER_CYCLE * DAY_MULTIPLIER));
        } else { // Nighttime
            world.setTime(time + (long) (NIGHT_MULTIPLIER * TICKS_PER_CYCLE));
        }
    }

}
