package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/*
This verifies... stuff
 */
public class SpellVerifyListener implements Listener {


    @EventHandler
    public void onEnemyVerifyEvent(EnemyVerifyEvent e) {

        Player caster = e.getCaster();
        Entity victim = e.getVictim();

        // bugfix for armor stands
        if (victim instanceof ArmorStand) e.setCancelled(true);

        // target must be alive
        if (!(victim instanceof LivingEntity)) e.setCancelled(true);
        LivingEntity livingVictim = (LivingEntity) victim;

        if (victim instanceof Horse && !MythicMobs.inst().getMobManager().isActiveMob(victim.getUniqueId())) e.setCancelled(true);

        // ignnore caster
        if (caster.equals(victim)) e.setCancelled(true);

        // ignore NPCs
        if (livingVictim.hasMetadata("NPC")) e.setCancelled(true);

        // skip party members
        if (victim instanceof Player) {
            if(RunicCore.getPartyManager().getPlayerParty(caster) != null
                    & !RunicCore.getPartyManager().getPlayerParty(caster).hasMember((Player) victim))
                e.setCancelled(true);
        }
    }
}
