package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.EnvironmentDamage;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GenericDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onGenericDamage(EnvironmentDamage event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player player)) return;
        DamageUtil.damageEntity(event.getAmount(), player);
        player.setNoDamageTicks(10);
    }
}
