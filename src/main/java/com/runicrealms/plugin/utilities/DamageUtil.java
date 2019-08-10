package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.outlaw.OutlawManager;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.listeners.DamageListener;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;

public class DamageUtil {

    public static void damageEntitySpell(double dmgAmt, LivingEntity recipient, Player caster, boolean halveGemBoost) {

        // update amount with gem values
        int gemBoost = GearScanner.getMagicBoost(caster);
        if (halveGemBoost) {
            gemBoost = gemBoost / 2;
        }
        dmgAmt = dmgAmt + gemBoost;

        // update w/ shield
        if (recipient instanceof Player) {
            int shieldAmt = GearScanner.getShieldAmt(((Player) recipient));
            dmgAmt = dmgAmt - shieldAmt;
        }

        // prevent healing
        if (dmgAmt < 0) {
            dmgAmt = 0;
        }

        // call our custom event, apply modifiers if necessary
        SpellDamageEvent event = new SpellDamageEvent((int) dmgAmt, recipient, caster);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        dmgAmt = event.getAmount();

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null
                && RunicCore.getPartyManager().getPlayerParty(caster).hasMember(recipient.getUniqueId())) { return; }

        // outlaw check
        if (recipient instanceof Player && (!OutlawManager.isOutlaw(((Player) recipient)) || !OutlawManager.isOutlaw(caster))) {
            return;
        }

        // apply the damage
        damageEntity(dmgAmt, recipient, caster, false);
        HologramUtil.createSpellDamageHologram((caster), recipient.getLocation().add(0,1.5,0), dmgAmt);
    }

    public static void damageEntityWeapon(double dmgAmt, LivingEntity recipient, Player caster, boolean isRanged) {

        // scan the gems
        dmgAmt = dmgAmt + GearScanner.getAttackDamage(caster);

        // update w/ shield
        if (recipient instanceof Player) {
            int shieldAmt = GearScanner.getShieldAmt(((Player) recipient));
            dmgAmt = dmgAmt - shieldAmt;
        }

        // prevent healing
        if (dmgAmt < 0) {
            dmgAmt = 0;
        }

        // call an event, apply modifiers if necessary
        WeaponDamageEvent event = new WeaponDamageEvent((int) dmgAmt, caster, recipient, isRanged);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        dmgAmt = event.getAmount();

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null
                && RunicCore.getPartyManager().getPlayerParty(caster).hasMember(recipient.getUniqueId())) { return; }

        // outlaw check
        if (recipient instanceof Player && (!OutlawManager.isOutlaw(((Player) recipient)) || !OutlawManager.isOutlaw(caster))) {
            return;
        }

        damageEntity(dmgAmt, recipient, caster, isRanged);
        HologramUtil.createDamageHologram((caster), recipient.getLocation().add(0,1.5,0), dmgAmt);
    }

    public static void damageEntityMob(double dmgAmt, LivingEntity recipient, Entity damager) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        mobDamage(dmgAmt, recipient, damager);
    }

    private static void mobDamage(double dmgAmt, LivingEntity recipient, Entity damager) {

        DamageListener damageListener = new DamageListener();

        if (MythicMobs.inst().getMobManager().isActiveMob(damager.getUniqueId())) {
            ActiveMob mm = MythicMobs.inst().getAPIHelper().getMythicMobInstance(damager);
            dmgAmt = mm.getDamage();
        }

        // update w/ shield
        if (recipient instanceof Player) {
            int shieldAmt = GearScanner.getShieldAmt(((Player) recipient));
            dmgAmt = dmgAmt - shieldAmt;
        }

        // prevent healing
        if (dmgAmt < 0) {
            dmgAmt = 0;
        }

        // todo: ensure it works w/ players. fix crafting tooltips for legendaries
        // call a custom damage event to communicate with other listeners/plugins
        EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(damager, recipient, EntityDamageEvent.DamageCause.CUSTOM, dmgAmt);
        Bukkit.getPluginManager().callEvent(e);
        recipient.setLastDamageCause(e);

        if (recipient instanceof Player) {
            KnockbackUtil.knockbackPlayer(damager, (Player) recipient);
        }

        // apply custom mechanics if the player were to die
        int newHP = (int) (recipient.getHealth() - dmgAmt);
        if (newHP >= 1) {
            if (recipient instanceof Monster) {
                Monster monster = (Monster) recipient;
                if (damager instanceof LivingEntity) {
                    monster.setTarget((LivingEntity) damager);
                }
            }
            recipient.setHealth(newHP);
            recipient.damage(0.0000000000001);
        } else if (recipient instanceof Player) {
            damageListener.applySlainMechanics(damager, (Player) recipient);
        } else {
            recipient.setHealth(0);
        }
    }

    private static void damageEntity(double dmgAmt, LivingEntity recipient, Player caster, boolean isRanged) {

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
            if (isRanged) {
                KnockbackUtil.knockbackRanged(caster, recipient);
            } else {
                KnockbackUtil.knockbackPlayer(caster, (Player) recipient);
            }
        } else {
            if (isRanged) {
                KnockbackUtil.knockbackRanged(caster, recipient);
            } else {
                KnockbackUtil.knockbackMob(caster, recipient);
            }
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


