package com.runicrealms.plugin.spellapi.spellutil;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class HealUtil  {

    /**
     * Our universal method to apply healing to a player using custom calculation.
     * @param healAmt amount to be healed before gem or buff calculations
     * @param recipient player to be healed
     * @param caster player who casted heal
     * @param isReducedOnCaster whether caster receives reduced healing
     * @param spell include a reference to spell for spell scaling
     */
    @SuppressWarnings("deprecation")
    public static void healPlayer(double healAmt, Player recipient, Player caster, boolean isReducedOnCaster, Spell... spell) {

        // spells are half effective on the caster
        if (isReducedOnCaster && recipient == caster) {
            healAmt = (healAmt/2);
        }

        // call our custom heal event for interaction with buffs/debuffs
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

            HologramUtil.createHealHologram(recipient, recipient.getLocation().add(0,1.5,0), difference);

        } else {

            recipient.setHealth(newHP);
            HologramUtil.createHealHologram(recipient, recipient.getLocation().add(0,1.5,0), healAmt);
        }
        recipient.playSound(recipient.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1);
        recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);

        // call a new health regen event to communicate with all the other events that depend on this.
        Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(recipient, healAmt, EntityRegainHealthEvent.RegainReason.CUSTOM));
    }

    /**
     *
     * @param shieldAmt amount to be shielded before gem or buff calculations
     * @param recipient player to recieve shield
     * @param caster player who casted shield
     * @param isReducedOnCaster whether caster should receive reduced gem bonus
     * @param spell include a reference to spell for spell scaling
     */
    public static void shieldPlayer(double shieldAmt, Player recipient, Player caster, boolean isReducedOnCaster, Spell... spell) {

        // spells are half effective on the caster
        if (isReducedOnCaster && recipient == caster) {
            shieldAmt = (shieldAmt / 2);
        }

        // call our custom heal event for interaction with buffs/debuffs todo: add customm shield event
//        SpellHealEvent event = new SpellHealEvent((int) shieldAmt, recipient, caster);
//        Bukkit.getPluginManager().callEvent(event);
//        if (event.isCancelled()) return;
//        shieldAmt = event.getAmount();

        RunicCore.getCombatManager().getShieldedPlayers().put(recipient.getUniqueId(), shieldAmt); // shields can't stack
        HologramUtil.createShieldHologram(recipient, recipient.getLocation().add(0, 1.5, 0), shieldAmt);
        recipient.playSound(recipient.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 0.5f);
        recipient.getWorld().spawnParticle(Particle.SPELL_INSTANT, recipient.getEyeLocation(), 25, 0.5F, 0.5F, 0.5F, 0);

        // call a new health regen event to communicate with all the other events that depend on this.
        //Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(recipient, shieldAmt, EntityRegainHealthEvent.RegainReas on.CUSTOM));
    }
}


