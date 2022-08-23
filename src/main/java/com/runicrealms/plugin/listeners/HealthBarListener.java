package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * This does...
 */
public class HealthBarListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onGenericDamage(SpellDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player)) return;
        Player player = (Player) event.getVictim();
        HologramUtil.createHealthBarHologram(player, player.getEyeLocation(), event.getAmount());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onGenericDamage(WeaponDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player)) return;
        Player player = (Player) event.getVictim();
        HologramUtil.createHealthBarHologram(player, player.getEyeLocation(), event.getAmount());
    }
}
