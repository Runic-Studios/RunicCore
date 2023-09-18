package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.EnvironmentDamageEvent;
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
    public void onEnvironmentDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        switch (event.getCause()) {
            case CONTACT, DROWNING, FIRE, HOT_FLOOR, LAVA -> {
                event.setCancelled(true);
                if (entity instanceof Player) {
                    createGenericDamageEvent((Player) entity, event.getCause(), event.getDamage());
                }
            }
            case FALL -> { // not cancelled for most mobs
                if (entity instanceof Player) {
                    event.setCancelled(true);
                    createGenericDamageEvent((Player) entity, event.getCause(), event.getDamage());
                }
            }
        }
    }

    private void createGenericDamageEvent(Player player, EntityDamageEvent.DamageCause cause, double eventDamage) {
        EnvironmentDamageEvent.DamageCauses damageCauses = EnvironmentDamageEvent.DamageCauses.getFromDamageCause(cause);
        EnvironmentDamageEvent environmentDamage = new EnvironmentDamageEvent
                (
                        player,
                        DamageEventUtil.calculateRunicDamageFromVanillaDamage(player, eventDamage, damageCauses),
                        damageCauses
                );
        Bukkit.getPluginManager().callEvent(environmentDamage);
    }
}
