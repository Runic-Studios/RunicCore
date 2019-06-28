package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.events.SpellDamageEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.listeners.DamageListener;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;

public class DamageUtil {

    public static void damageEntitySpell(double dmgAmt, LivingEntity recipient, Player caster) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null
                && RunicCore.getPartyManager().getPlayerParty(caster).hasMember(recipient.getUniqueId())) { return; }

        // update amount with gem values
        dmgAmt = dmgAmt + GearScanner.getMagicBoost(caster);

        // call our custom event, apply modifiers if necessary
        SpellDamageEvent event = new SpellDamageEvent((int) dmgAmt, recipient, caster);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        dmgAmt = event.getAmount();

        // apply the damage
        damageEntity(dmgAmt, recipient, caster);
        HologramUtil.createSpellDamageHologram((caster), recipient.getLocation().add(0,1.5,0), dmgAmt);
    }

    public static void damageEntityWeapon(double dmgAmt, LivingEntity recipient, Player caster) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null
                && RunicCore.getPartyManager().getPlayerParty(caster).hasMember(recipient.getUniqueId())) { return; }

        // scan the gems
        dmgAmt = dmgAmt + GearScanner.getAttackDamage(caster);
        damageEntity(dmgAmt, recipient, caster);
        HologramUtil.createDamageHologram((caster), recipient.getLocation().add(0,1.5,0), dmgAmt);
    }

    public static void damageEntityMob(double dmgAmt, LivingEntity recipient, LivingEntity damager) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        mobDamage(dmgAmt, recipient, damager);
    }

    private static void mobDamage(double dmgAmt, LivingEntity recipient, LivingEntity damager) {
        DamageListener damageListener = new DamageListener();

        int newHP = (int) (recipient.getHealth() - dmgAmt);

        // call a custom damage event to communicate with other listeners/plugins
        EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(damager, recipient, EntityDamageEvent.DamageCause.CUSTOM, dmgAmt);
        Bukkit.getPluginManager().callEvent(e);
        recipient.setLastDamageCause(e);

        if (recipient instanceof Player) {
            KnockbackUtil.knockback(damager, recipient);
        }

        // apply custom mechanics if the player were to die
        if (newHP >= 1) {
            if (recipient instanceof Monster) {
                Monster monster = (Monster) recipient;
                monster.setTarget(damager);
            }
            recipient.setHealth(newHP);
            recipient.damage(0.0000000000001);
        } else if (recipient instanceof Player) {
            damageListener.applySlainMechanics(damager, (Player) recipient);
        } else {
            recipient.setHealth(0);
        }
    }

    private static void damageEntity(double dmgAmt, LivingEntity recipient, Player caster) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        // caster can't damage themselves
        if (recipient == caster) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null
                && RunicCore.getPartyManager().getPlayerParty(caster).hasMember(recipient.getUniqueId())) { return; }

        DamageListener damageListener = new DamageListener();

        int newHP = (int) (recipient.getHealth() - dmgAmt);

        // call a custom damage event to communicate with other listeners/plugins
        EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(caster, recipient, EntityDamageEvent.DamageCause.CUSTOM, dmgAmt);
        Bukkit.getPluginManager().callEvent(e);
        recipient.setLastDamageCause(e);

        if (recipient instanceof Player) {
            KnockbackUtil.knockback(caster, recipient);
        } else if (recipient instanceof Monster) {
            KnockbackUtil.knockbackMob(caster, recipient);
        }

        // apply custom mechanics if the player were to die
        if (newHP >= 1) {
            if (recipient instanceof Monster) {
                Monster monster = (Monster) recipient;
                monster.setTarget(caster);
            }
            recipient.setHealth(newHP);
            recipient.damage(0.0000000000001);
        } else if (recipient instanceof Player) {
            damageListener.applySlainMechanics(caster, (Player) recipient);
        } else {
            recipient.setHealth(0);
        }
    }
}


