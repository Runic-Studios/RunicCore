package com.runicrealms.plugin.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import com.runicrealms.plugin.RunicCore;

public class PlayerRegenListener implements Listener {

    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {

        if (!(e.getEntity() instanceof Player)) return;

        if (!(e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED)) return;

        Player pl = (Player) e.getEntity();

        CombatManager cm = RunicCore.getCombatManager();
        if (cm.getPlayersInCombat().containsKey(pl.getUniqueId())) {
            e.setCancelled(true);
        } /*else {
            e.setAmount(5); // after we remove old combat mechanics
        }
        */
    }
}
