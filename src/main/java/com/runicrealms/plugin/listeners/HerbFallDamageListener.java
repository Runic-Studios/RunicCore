package com.runicrealms.plugin.listeners;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class HerbFallDamageListener implements Listener {

    @EventHandler(priority = EventPriority.LOW) // early
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        if (event.getEntity() instanceof Player) return;
        if (!MythicMobs.inst().getMobManager().isActiveMob(event.getEntity().getUniqueId())) return;
        if (MythicMobs.inst().getMobManager().getActiveMob(event.getEntity().getUniqueId()).isPresent()) {
            ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(event.getEntity().getUniqueId()).get();
            if (am.hasFaction() && am.getFaction().equalsIgnoreCase("herb")) {
                event.setCancelled(true);
            }
        }
    }

}
