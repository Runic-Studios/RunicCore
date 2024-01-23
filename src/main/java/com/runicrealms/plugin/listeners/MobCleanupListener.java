package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.runicrestart.event.PreShutdownEvent;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Sometimes when disguises break, mobs will not get removed. For example, Azana guards becoming iron golems
 * This removes them on shutdown
 *
 * @author Skyfallin
 */
public class MobCleanupListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShutdown(PreShutdownEvent e) {
        // Clear all mobs
        MythicBukkit.inst().getMobManager().despawnAllMobs();
    }

}
