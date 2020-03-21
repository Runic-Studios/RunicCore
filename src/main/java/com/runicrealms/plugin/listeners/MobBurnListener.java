package com.runicrealms.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;

public class MobBurnListener implements Listener {
    @EventHandler
    public void onBurn(EntityCombustEvent event){
        event.setCancelled(true);
    }
}
