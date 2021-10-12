package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.events.HealthRegenEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@SuppressWarnings("deprecation")
public class PlayerRegenListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onRegen(HealthRegenEvent e) {
        if (e.isCancelled()) return;
        double maxHealth = e.getPlayer().getMaxHealth();
        int amount = e.getAmount();
        if ((e.getPlayer().getHealth() + amount) > maxHealth)
            e.getPlayer().setHealth(e.getPlayer().getMaxHealth());
        else
            e.getPlayer().setHealth(e.getPlayer().getHealth() + e.getAmount());
    }
}
