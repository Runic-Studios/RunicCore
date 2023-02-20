package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GenericDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onGenericDamage(GenericDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getVictim() instanceof Player)) return;
        Player player = (Player) event.getVictim();
        DamageUtil.damagePlayer(event.getAmount(), player);
        player.setNoDamageTicks(10);
    }
}
