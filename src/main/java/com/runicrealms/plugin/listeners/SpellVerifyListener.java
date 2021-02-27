package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/*
 * This listener is called when a spell attempts to activate the spell logic on an entity.
 * Used to prevent non-pvpers from taking damage/effects (See RunicPvP)
 */
public class SpellVerifyListener implements Listener {


    @EventHandler
    public void onEnemyVerifyEvent(EnemyVerifyEvent e) {

        Player caster = e.getCaster();
        Entity victim = e.getVictim();

        // bugfix for armor stands
        if (victim instanceof ArmorStand) {
            e.setCancelled(true);
            return;
        }

        // target must be alive
        if (!(victim instanceof LivingEntity)) {
            e.setCancelled(true);
            return;
        }

        LivingEntity livingVictim = (LivingEntity) victim;

        if (victim instanceof Horse && !MythicMobs.inst().getMobManager().isActiveMob(victim.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        // ignnore caster
        if (caster.equals(victim)) {
            e.setCancelled(true);
            return;
        }

        // ignore NPCs
        if (livingVictim.hasMetadata("NPC")) {
            e.setCancelled(true);
            return;
        }

        // skip party members
//        if (victim instanceof Player) {
//            if(RunicCore.getPartyManager().getPlayerParty(caster) != null
//                    & !RunicCore.getPartyManager().getPlayerParty(caster).hasMember((Player) victim))
//                e.setCancelled(true);
//        }
        if (!(victim instanceof Player)) return;
        if (RunicCoreAPI.isPartyMember(caster, (Player) victim))
            e.setCancelled(true);
    }
}
