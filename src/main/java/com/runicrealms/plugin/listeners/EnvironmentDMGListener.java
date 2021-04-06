package com.runicrealms.plugin.listeners;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EnvironmentDMGListener implements Listener {

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        if (MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).isPresent()) {
            ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).get();
            if (am.hasFaction() && am.getFaction().equalsIgnoreCase("herb")) {
                e.setCancelled(true);
            }
        } // todo: ELSE handle fall damgage, lava, fire, etc.
    }

    /*
    Fireworks
     */
    @EventHandler
    public void onFireworkDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Firework)
            e.setCancelled(true);
    }
}
