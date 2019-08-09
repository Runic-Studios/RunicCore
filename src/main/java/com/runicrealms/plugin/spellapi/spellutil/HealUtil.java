package com.runicrealms.plugin.spellapi.spellutil;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.HashMap;
import java.util.UUID;

public class HealUtil  {

    private static HashMap<UUID, Integer> shieldedPlayers = new HashMap<>();

    @SuppressWarnings("deprecation")
    public static void healPlayer(double healAmt, Player recipient, Player caster,
                                  boolean gemBoosted, boolean halveGemBoost, boolean isReducedOnCaster) {

        // scan for gem values
        if (gemBoosted) {
            int boost = GearScanner.getHealingBoost(caster);
            if (halveGemBoost) {
                boost = boost/2;
            }
            healAmt = healAmt + boost;
        }

        // spells are half effective on the caster
        if (isReducedOnCaster && recipient == caster) {
            healAmt = (healAmt/2);
        }

        // call our custom heal event for interaction with buffs/debuffs
        SpellHealEvent event = new SpellHealEvent((int) healAmt, recipient, caster);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        healAmt = event.getAmount();

        // skip the player if they're not in the party
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null
                && !RunicCore.getPartyManager().getPlayerParty(caster).hasMember(recipient.getUniqueId())) {
            return;
        }

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
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1);
            recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);

        } else {

            recipient.setHealth(newHP);
            HologramUtil.createHealHologram(recipient, recipient.getLocation().add(0,1.5,0), healAmt);
            recipient.playSound(recipient.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1);
            recipient.getWorld().spawnParticle(Particle.HEART, recipient.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);
        }

        // call a new health regen event to communicate with all the other events that depend on this.
        Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(recipient, healAmt, EntityRegainHealthEvent.RegainReason.CUSTOM));
    }

    //todo: create shield event
    public static void shieldPlayer(double shieldAmt, Player recipient, Player caster) {

        // scan for gem values
        shieldAmt = shieldAmt + GearScanner.getHealingBoost(caster);

        HologramUtil.createShieldHologram(recipient, recipient.getLocation().add(0,1.5,0), shieldAmt);
        recipient.playSound(recipient.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
        recipient.playSound(recipient.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1);
        recipient.getWorld().spawnParticle(Particle.SPELL_INSTANT, recipient.getEyeLocation(), 5, 0, 0.5F, 0.5F, 0.5F);
        shieldedPlayers.put(recipient.getUniqueId(), (int) shieldAmt);
    }

    public static HashMap<UUID, Integer> getShieldedPlayers() {
        return shieldedPlayers;
    }
}


