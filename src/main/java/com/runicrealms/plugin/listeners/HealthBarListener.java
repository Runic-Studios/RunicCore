package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Updates mob health bars when they take damage
 */
public class HealthBarListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onGenericDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player)) return;
        Player victim = (Player) event.getVictim();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HologramUtil.createHealthBarHologram(event.getPlayer(), victim, victim.getEyeLocation()), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onGenericDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player)) return;
        Player victim = (Player) event.getVictim();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HologramUtil.createHealthBarHologram(event.getPlayer(), victim, victim.getEyeLocation()), 1L);
    }
}
