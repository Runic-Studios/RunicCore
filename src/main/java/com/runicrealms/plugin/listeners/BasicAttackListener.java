package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.api.event.BasicAttackEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BasicAttackListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBasicAttackEvent(BasicAttackEvent event) {
        if (event.isCancelled()) return;
        event.getPlayer().setCooldown(event.getMaterial(), event.getCooldownTicks());
    }

}
