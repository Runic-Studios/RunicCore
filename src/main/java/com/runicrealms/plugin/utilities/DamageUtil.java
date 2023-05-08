package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.RangedDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.listeners.DamageListener;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.KnockbackUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collections;

public class DamageUtil {

    public static void damageEntitySpell(double dmgAmt, LivingEntity recipient, Player caster, Spell... spell) {
        damageEntitySpell(dmgAmt, recipient, caster, true, spell);
    }

    /**
     * Our universal method to apply magic damage to a player using custom calculation.
     *
     * @param dmgAmt    amount to be dealt before gem or buff calculations
     * @param recipient player to be healed
     * @param caster    player who cast the healing spell
     * @param spell     include a reference to spell for spell scaling
     */
    public static void damageEntitySpell(double dmgAmt, LivingEntity recipient, Player caster, boolean knockback, Spell... spell) {
        // Prevent healing
        if (dmgAmt < 0) {
            dmgAmt = 0;
        }

        // Call our custom event
        MagicDamageEvent event = new MagicDamageEvent((int) dmgAmt, recipient, caster, spell);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        dmgAmt = event.getAmount();

        // Skip party members
        if (RunicCore.getPartyAPI().getParty(caster.getUniqueId()) != null) {
            if (recipient instanceof Player) {
                if (RunicCore.getPartyAPI().getParty(caster.getUniqueId()).hasMember((Player) recipient)) {
                    return;
                }
            }
        }

        // Apply the damage
        damageEntityByEntity(dmgAmt, recipient, caster, false, knockback);
        ChatColor chatColor = event.isCritical() ? ChatColor.GOLD : ChatColor.DARK_AQUA;
        HologramUtil.createCombatHologram(Collections.singletonList(caster), recipient.getEyeLocation(), chatColor + "-" + (int) dmgAmt + " ❤ʔ");
    }

    /**
     * Our universal method to apply physical damage to a player using custom calculation.
     *
     * @param dmgAmt        amount to be healed before gem or buff calculations
     * @param recipient     player to be healed
     * @param caster        player who cast the healing spell
     * @param isBasicAttack whether the attack will be treated as an basic attack (for on-hit effects)
     * @param isRanged      whether the attack is ranged
     * @param spell         include a reference to spell for spell scaling
     */
    public static void damageEntityPhysical(double dmgAmt, LivingEntity recipient, Player caster,
                                            boolean isBasicAttack, boolean isRanged, Spell... spell) {
        // prevent healing
        if (dmgAmt < 0) {
            dmgAmt = 0;
        }

        // call an event, apply modifiers if necessary
        PhysicalDamageEvent event = new PhysicalDamageEvent((int) dmgAmt, caster, recipient, isBasicAttack, isRanged, spell);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        dmgAmt = event.getAmount();

        // skip party members
        if (RunicCore.getPartyAPI().getParty(caster.getUniqueId()) != null) {
            if (recipient instanceof Player) {
                if (RunicCore.getPartyAPI().getParty(caster.getUniqueId()).hasMember((Player) recipient)) {
                    return;
                }
            }
        }

        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1.0f);
        damageEntityByEntity(dmgAmt, recipient, caster, isRanged);
        ChatColor chatColor = event.isCritical() ? ChatColor.GOLD : ChatColor.RED;
        HologramUtil.createCombatHologram(Collections.singletonList(caster), recipient.getEyeLocation(), chatColor + "-" + (int) dmgAmt + " ❤⚔");
    }

    public static void damageEntityRanged(double dmgAmt, LivingEntity recipient, Player caster,
                                          boolean isBasicAttack, Arrow arrow, Spell... spell) {
        // prevent healing
        if (dmgAmt < 0)
            dmgAmt = 0;

        // call an event, apply modifiers if necessary
        PhysicalDamageEvent event = new RangedDamageEvent((int) dmgAmt, caster, recipient, isBasicAttack, arrow, spell);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        dmgAmt = event.getAmount();

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        // skip party members
        if (RunicCore.getPartyAPI().getParty(caster.getUniqueId()) != null) {
            if (recipient instanceof Player) {
                if (RunicCore.getPartyAPI().getParty(caster.getUniqueId()).hasMember((Player) recipient)) {
                    return;
                }
            }
        }

        damageEntityByEntity(dmgAmt, recipient, caster, true);
        ChatColor chatColor = event.isCritical() ? ChatColor.GOLD : ChatColor.RED;
        HologramUtil.createCombatHologram(Collections.singletonList(caster), recipient.getEyeLocation(), chatColor + "-" + (int) dmgAmt + " ❤⚔");
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
            KnockbackUtil.knockbackMeleePlayer(damager, (Player) recipient);
        }

        // apply custom mechanics if the player were to die
        int newHP = (int) (recipient.getHealth() - dmgAmt);
        if (newHP >= 1) {
            if (recipient instanceof Monster monster) {
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

    private static void damageEntityByEntity(double dmgAmt, LivingEntity recipient, Player caster, boolean isRanged) {
        damageEntityByEntity(dmgAmt, recipient, caster, isRanged, true);
    }

    private static void damageEntityByEntity(double dmgAmt, LivingEntity recipient, Player caster, boolean isRanged, boolean knockback) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        // caster can't damage themselves
        if (recipient == caster) return;

        // skip party members
        if (RunicCore.getPartyAPI().getParty(caster.getUniqueId()) != null) {
            if (recipient instanceof Player) {
                if (RunicCore.getPartyAPI().getParty(caster.getUniqueId()).hasMember((Player) recipient)) {
                    return;
                }
            }
        }

        int newHP = (int) (recipient.getHealth() - dmgAmt);

        // call a custom damage event to communicate with other listeners/plugins
        EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(caster, recipient, EntityDamageEvent.DamageCause.CUSTOM, dmgAmt);
        Bukkit.getPluginManager().callEvent(e);
        recipient.setLastDamageCause(e);

        if (recipient instanceof Player && knockback) {
            if (isRanged) {
                KnockbackUtil.knockbackRangedPlayer(caster, (Player) recipient);
            } else {
                KnockbackUtil.knockbackMeleePlayer(caster, (Player) recipient);
            }
        } else if (knockback) {
            KnockbackUtil.knockBackMob(caster, recipient, isRanged);
        }

        // apply custom mechanics if the player were to die
        if (newHP >= 1) {
            if (recipient instanceof Monster monster) {
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

    /**
     * This method damages a player using custom runic mechanics
     *
     * @param dmgAmt damage amount to deal
     * @param victim to receive damage
     */
    public static void damagePlayer(double dmgAmt, Player victim) {
        int newHP = (int) (victim.getHealth() - dmgAmt);

        // call a custom damage event to communicate with other listeners/plugins
        EntityDamageEvent e = new EntityDamageEvent(victim, EntityDamageEvent.DamageCause.CUSTOM, dmgAmt);
        Bukkit.getPluginManager().callEvent(e);
        victim.setLastDamageCause(e);

        // apply knock back
        victim.setVelocity(victim.getLocation().getDirection().multiply(DamageEventUtil.getEnvironmentKnockbackMultiplier()));

        // apply custom mechanics if the player were to die
        if (newHP >= 1) {
            victim.setHealth(newHP);
            victim.setNoDamageTicks(0);
            victim.damage(0.0000000000001);
        } else {
            RunicDeathEvent runicDeathEvent = new RunicDeathEvent(victim, victim.getLocation());
            Bukkit.getPluginManager().callEvent(runicDeathEvent);
        }
        HologramUtil.createCombatHologram(null, victim.getEyeLocation(), ChatColor.RED + "-" + (int) dmgAmt + " ❤");
    }

}


