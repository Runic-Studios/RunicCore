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
            case FIRE:
            case HOT_FLOOR:
            case LAVA:
                e.setCancelled(true);
                if (entity instanceof Player) {
                    createGenericDamageEvent((Player) entity, e.getCause(), e.getDamage());
                }
                break;
            case FALL: // not cancelled for most mobs
                if (entity instanceof Player) {
                    e.setCancelled(true);
                    createGenericDamageEvent((Player) entity, e.getCause(), e.getDamage());
                }
                break;
        }
    }

    private void createGenericDamageEvent(Player player, EntityDamageEvent.DamageCause cause, double eventDamage) {
        GenericDamageEvent.DamageCauses damageCauses = GenericDamageEvent.DamageCauses.getFromDamageCause(cause);
        GenericDamageEvent genericDamageEvent = new GenericDamageEvent
                (
                        player,
                        DamageEventUtil.calculateRunicDamageFromVanillaDamage(player, eventDamage, damageCauses),
                        damageCauses
                );
        Bukkit.getPluginManager().callEvent(genericDamageEvent);
    }
}
