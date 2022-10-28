package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.utilities.DamageEventUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EnvironmentDamageListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST) // very early
    public void onEnvironmentDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        switch (e.getCause()) {
            case CONTACT:
            case DROWNING:
            case FALL:
            case FIRE:
            case HOT_FLOOR:
            case LAVA:
                e.setCancelled(true);
                if (entity instanceof Player) {
                    GenericDamageEvent.DamageCauses damageCauses = GenericDamageEvent.DamageCauses.getFromDamageCause(e.getCause());
                    GenericDamageEvent genericDamageEvent = new GenericDamageEvent
                            (
                                    (Player) entity,
                                    DamageEventUtil.calculateRunicDamageFromVanillaDamage((Player) entity, e.getDamage(), damageCauses),
                                    damageCauses
                            );
                    Bukkit.getPluginManager().callEvent(genericDamageEvent);
                }
                break;
        }
    }
}
