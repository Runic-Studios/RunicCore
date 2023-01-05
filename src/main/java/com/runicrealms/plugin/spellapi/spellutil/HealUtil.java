package com.runicrealms.plugin.spellapi.spellutil;

import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class HealUtil {

    /**
     * Our universal method to apply healing to a player using custom calculation.
     *
     * @param healAmt           amount to be healed before gem or buff calculations
     * @param recipient         player to be healed
     * @param caster            player who cast heal
     * @param isReducedOnCaster whether caster receives reduced healing
     * @param spell             include a reference to spell for spell scaling
     */
    @SuppressWarnings("deprecation")
    public static void healPlayer(double healAmt, Player recipient, Player caster, boolean isReducedOnCaster, Spell... spell) {

        // spells are half effective on the caster
        if (isReducedOnCaster && recipient == caster) {
            healAmt = (healAmt / 2);
        }

        // call our custom heal event for interaction with buffs/de buffs
        SpellHealEvent event = spell.length > 0
                ? new SpellHealEvent((int) healAmt, recipient, caster, spell)
                : new SpellHealEvent((int) healAmt, recipient, caster);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        healAmt = event.getAmount();

        double newHP = recipient.getHealth() + healAmt;
        double difference = recipient.getMaxHealth() - recipient.getHealth();
        // if they are missing less than healAmt
        if (newHP > recipient.getMaxHealth()) {

            recipient.setHealth(recipient.getMaxHealth());

            if (difference == (int) difference) {
                if (difference <= 0) {
                    return;
                }
            }

            HologramUtil.createHealHologram(recipient, recipient.getLocation().add(0, 1.5, 0), difference, event.isCritical());

        } else {

            recipient.setHealth(newHP);
            HologramUtil.createHealHologram(recipient, recipient.getLocation().add(0, 1.5, 0), healAmt, event.isCritical());
        }
        recipient.playSound(recipient.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f, 0.5f);
        recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 3, 0.35F, 0.35F, 0.35F, 0);

        // call a new health regen event to communicate with all the other events that depend on this.
        Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(recipient, healAmt, EntityRegainHealthEvent.RegainReason.CUSTOM));
    }

}


