package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GenericDamageListener implements Listener {

    @EventHandler
    public void onGenericDamage(GenericDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getVictim() instanceof Player)) return;
        Player player = (Player) e.getVictim();
        DamageUtil.damagePlayer(e.getAmount(), player);
        player.setNoDamageTicks(10);
    }
}
