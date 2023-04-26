package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * This listener is called when a spell attempts to activate the spell logic on an entity.
 * Used to prevent non-pvpers from taking damage/effects (See RunicPvP)
 */
public class EnemyVerifyListener implements Listener {


    @EventHandler
    public void onEnemyVerifyEvent(EnemyVerifyEvent event) {

        Player caster = event.getCaster();
        Entity victim = event.getVictim();

        // bugfix for armor stands
        if (victim instanceof ArmorStand) {
            event.setCancelled(true);
            return;
        }

        // target must be alive
        if (!(victim instanceof LivingEntity livingVictim)) {
            event.setCancelled(true);
            return;
        }

        if (victim instanceof Horse && !MythicMobs.inst().getMobManager().isActiveMob(victim.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // ignore caster
        if (caster.equals(victim)) {
            event.setCancelled(true);
            return;
        }

        // ignore NPCs
        if (livingVictim.hasMetadata("NPC")) {
            event.setCancelled(true);
            return;
        }
        if (!(victim instanceof Player)) return;

        // skip party members
        if (RunicCore.getPartyAPI().isPartyMember(caster.getUniqueId(), (Player) victim))
            event.setCancelled(true);
    }
}
