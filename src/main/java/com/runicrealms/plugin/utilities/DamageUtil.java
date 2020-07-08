package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.listeners.DamageListener;
import com.runicrealms.plugin.player.outlaw.OutlawManager;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageUtil {

    public static void damageEntitySpell(double dmgAmt, LivingEntity recipient, Player caster, double gemBoostPercent) {

        // update amount with gem values
        int gemBoost = GearScanner.getMagicBoost(caster);
        if (gemBoostPercent < 100) {
            gemBoost = (int) (gemBoost * (gemBoostPercent / 100));
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

        // todo: this needs to go inside spell, because this is wrong
        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        //if (recipient instanceof ArmorStand) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null) {
            if (recipient instanceof Player) {
                if (RunicCore.getPartyManager().getPlayerParty(caster).hasMember((Player) recipient)) {
                    return;
                }
            }
        }

        // outlaw check
        if (recipient instanceof Player && (!OutlawManager.isOutlaw(((Player) recipient)) || !OutlawManager.isOutlaw(caster))) {
            return;
        }

        // apply the damage
        damageEntity(dmgAmt, recipient, caster, false);
        HologramUtil.createSpellDamageHologram((caster), recipient.getLocation().add(0,1.5,0), dmgAmt);
    }

    public static void damageEntityWeapon(double dmgAmt, LivingEntity recipient, Player caster, boolean isRanged, boolean bypassNoTick) {

        // no damage ticks delay
        if (!bypassNoTick && recipient.getNoDamageTicks() > 0) return;

        // scan the gems
        dmgAmt = dmgAmt + GearScanner.getAttackBoost(caster);

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
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null) {
            if (recipient instanceof Player) {
                if (RunicCore.getPartyManager().getPlayerParty(caster).hasMember((Player) recipient)) {
                    return;
                }
            }
        }

        // outlaw check
        if (recipient instanceof Player && (!OutlawManager.isOutlaw(((Player) recipient)) || !OutlawManager.isOutlaw(caster))) {
            return;
        }

        damageEntity(dmgAmt, recipient, caster, isRanged);
        HologramUtil.createDamageHologram((caster), recipient.getLocation().add(0,1.5,0), dmgAmt);
    }

    public static void damageEntityMob(double dmgAmt, LivingEntity recipient, Entity damager, boolean knockBack) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        mobDamage(dmgAmt, recipient, damager, knockBack);
    }

    private static void mobDamage(double dmgAmt, LivingEntity recipient, Entity damager, boolean knockBack) {

        /*
        Calculated in Damage Listener now so this doesn't override debuffs from spells.
         */
//        if (MythicMobs.inst().getMobManager().isActiveMob(damager.getUniqueId())) {
//            ActiveMob mm = MythicMobs.inst().getAPIHelper().getMythicMobInstance(damager);
//            dmgAmt = mm.getDamage();
//        }

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
//        EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(damager, recipient, EntityDamageEvent.DamageCause.CUSTOM, dmgAmt);
//        Bukkit.getPluginManager().callEvent(e);
//        recipient.setLastDamageCause(e);

        if (recipient instanceof Player && knockBack) {
            KnockbackUtil.knockbackPlayer(damager, (Player) recipient);
        }

        // apply custom mechanics if the player were to die
        int newHP = (int) (recipient.getHealth() - dmgAmt);
        if (newHP >= 1) {
            if (recipient instanceof Monster) {
                Monster monster = (Monster) recipient;
                if (damager instanceof LivingEntity) {
                    if (monster.getTarget() == null) monster.setTarget((LivingEntity) damager);
                }
            }
            recipient.setHealth(newHP);
            recipient.damage(0.0000000000001);
        } else if (recipient instanceof Player) {
            DamageListener.applySlainMechanics(damager, (Player) recipient);
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
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null) {
            if (recipient instanceof Player) {
                if (RunicCore.getPartyManager().getPlayerParty(caster).hasMember((Player) recipient)) {
                    return;
                }
            }
        }

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
                if (monster.getTarget() == null) monster.setTarget(caster);
            }
            recipient.setHealth(newHP);
            recipient.setNoDamageTicks(0);
            recipient.damage(0.0000000000001);
        } else if (recipient instanceof Player) {
            DamageListener.applySlainMechanics(caster, (Player) recipient);
        } else {
            recipient.setHealth(0);
        }
    }
}


