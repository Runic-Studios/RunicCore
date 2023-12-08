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
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class DamageUtil {
    private static final double CRITICAL_MULTIPLIER = 1.5;

    public static void damageEntitySpell(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster, @Nullable Spell spell) {
        damageEntitySpell(dmgAmt, recipient, caster, true, spell);
    }

    public static void damageEntitySpell(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster) {
        damageEntitySpell(dmgAmt, recipient, caster, null);
    }

    /**
     * Our universal method to apply magic damage to a player using custom calculation.
     *
     * @param dmgAmt    amount to be dealt before gem or buff calculations
     * @param recipient player to be healed
     * @param caster    player who cast the healing spell
     * @param spell     include a reference to spell for spell scaling
     */
    public static void damageEntitySpell(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster, boolean knockback, @Nullable Spell spell) {
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
        double finalDamage = event.isCritical() ? (dmgAmt * CRITICAL_MULTIPLIER) : dmgAmt;
        damageEntityByEntity(finalDamage, recipient, caster, false, knockback);
        ChatColor chatColor = event.isCritical() ? ChatColor.GOLD : ChatColor.DARK_AQUA;
        HologramUtil.createCombatHologram(Collections.singletonList(caster), recipient.getEyeLocation(), chatColor + "-" + (int) finalDamage + " ❤ʔ");
    }

    public static void damageEntitySpell(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster, boolean knockback) {
        damageEntitySpell(dmgAmt, recipient, caster, knockback, null);
    }

    public static void damageEntityPhysical(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster,
                                            boolean isBasicAttack, boolean isRanged, @Nullable Spell spell) {
        damageEntityPhysical(dmgAmt, recipient, caster, isBasicAttack, isRanged, true, spell);
    }

    public static void damageEntityPhysical(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster,
                                            boolean isBasicAttack, boolean isRanged) {
        damageEntityPhysical(dmgAmt, recipient, caster, isBasicAttack, isRanged, null);
    }

    public static void damageEntityPhysical(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster,
                                            boolean isBasicAttack, boolean isRanged, boolean knockback) {
        damageEntityPhysical(dmgAmt, recipient, caster, isBasicAttack, isRanged, knockback, null);
    }

    /**
     * Our universal method to apply physical damage to a player using custom calculation.
     *
     * @param dmgAmt        amount to be healed before gem or buff calculations
     * @param recipient     player to be healed
     * @param caster        player who cast the healing spell
     * @param isBasicAttack whether the attack will be treated as an basic attack (for on-hit effects)
     * @param isRanged      whether the attack is ranged
     * @param knockback     whether to apply knockback
     * @param spell         include a reference to spell for spell scaling
     */
    public static void damageEntityPhysical(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster,
                                            boolean isBasicAttack, boolean isRanged, boolean knockback, @Nullable Spell spell) {
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

        // Apply the damage
        double finalDamage = event.isCritical() ? (dmgAmt * CRITICAL_MULTIPLIER) : dmgAmt;
        damageEntityByEntity(finalDamage, recipient, caster, isRanged, knockback);
        ChatColor chatColor = event.isCritical() ? ChatColor.GOLD : ChatColor.RED;
        HologramUtil.createCombatHologram(Collections.singletonList(caster), recipient.getEyeLocation(), chatColor + "-" + (int) finalDamage + " ❤⚔");
    }

    public static void damageEntityRanged(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster,
                                          boolean isBasicAttack, @NotNull Arrow arrow, @Nullable Spell spell) {
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

    public static void damageEntityRanged(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster,
                                          boolean isBasicAttack, @NotNull Arrow arrow) {
        damageEntityRanged(dmgAmt, recipient, caster, isBasicAttack, arrow, null);
    }

    public static void damageEntityMob(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Entity damager, boolean knockBack) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        mobDamage(dmgAmt, recipient, damager, knockBack);
    }

    private static void mobDamage(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Entity damager, boolean knockBack) {

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

    private static void damageEntityByEntity(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster, boolean isRanged) {
        damageEntityByEntity(dmgAmt, recipient, caster, isRanged, true);
    }

    private static void damageEntityByEntity(double dmgAmt, @NotNull LivingEntity recipient, @NotNull Player caster, boolean isRanged, boolean knockback) {

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
     * @param dmgAmt    damage amount to deal
     * @param victim    to receive damage
     * @param knockback if the victim should be knocked back
     */
    public static void damageEntityGeneric(double dmgAmt, @NotNull LivingEntity victim, boolean knockback) {
        int newHP = (int) (victim.getHealth() - Math.max(dmgAmt, 0));

        // call a custom damage event to communicate with other listeners/plugins
        EntityDamageEvent e = new EntityDamageEvent(victim, EntityDamageEvent.DamageCause.CUSTOM, Math.max(dmgAmt, 0));
        Bukkit.getPluginManager().callEvent(e);
        victim.setLastDamageCause(e);

        // apply knock back
        if (knockback) {
            victim.setVelocity(victim.getLocation().getDirection().multiply(DamageEventUtil.getEnvironmentKnockbackMultiplier()));
        }

        // apply custom mechanics if the player were to die
        if (newHP >= 1) {
            if (newHP <= victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                victim.setHealth(newHP);
                victim.setNoDamageTicks(0);
                victim.damage(0.0000000000001);
            }
        } else if (victim instanceof Player player) {
            RunicDeathEvent runicDeathEvent = new RunicDeathEvent(player, victim.getLocation());
            Bukkit.getPluginManager().callEvent(runicDeathEvent);
        } else {
            victim.setHealth(0);
        }

        HologramUtil.createCombatHologram(null, victim.getEyeLocation(), ChatColor.RED + "-" + (int) dmgAmt + " ❤");
    }

    /**
     * This method damages a player using custom runic mechanics
     *
     * @param dmgAmt damage amount to deal
     * @param victim to receive damage
     */
    public static void damageEntityGeneric(double dmgAmt, @NotNull LivingEntity victim) {
        damageEntityGeneric(dmgAmt, victim, true);
    }
}


