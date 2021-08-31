package com.runicrealms.plugin.listeners;

import com.runicrealms.runicrestart.api.ServerShutdownEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
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

    private static final String world = "Alterra";

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShutdown(ServerShutdownEvent e) {
        if (Bukkit.getWorld(world) == null) return;
        for (Entity en : Bukkit.getWorld(world).getEntities()) {
            if (!(en instanceof LivingEntity)) continue;
            if (en instanceof Player) continue;
            if (!MythicMobs.inst().getMobManager().isActiveMob(en.getUniqueId())) {
                en.remove();
            }
        }
    }
}
