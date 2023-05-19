package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.AllyVerifyEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AllyVerifyListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL) // "middle" priority
    public void onAllyVerify(AllyVerifyEvent event) {
        Entity entity = event.getRecipient();
        // target must be a player
        if (!(entity instanceof Player target)) {
            event.setCancelled(true);
            return;
        }
        // If caster has a party, only party members count as ally
        if (RunicCore.getPartyAPI().hasParty(event.getCaster().getUniqueId())
                && !RunicCore.getPartyAPI().isPartyMember(event.getCaster().getUniqueId(), target)) {
            event.setCancelled(true);
        }
    }

}
