package com.runicrealms.plugin.utilities;

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
import com.runicrealms.plugin.spellapi.spelltypes.skillutil.KnockbackUtil;

public class DamageUtil {

    public static void damageEntityMagic(double dmgAmt, LivingEntity recipient, Player caster) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null
                && RunicCore.getPartyManager().getPlayerParty(caster).hasMember(recipient.getUniqueId())) { return; }

        dmgAmt = dmgAmt + GearScanner.getMagicBoost(caster);
        damageEntity(dmgAmt, recipient, caster);
        HologramUtil.createSkillDamageHologram((caster), recipient.getLocation().add(0,1.5,0), dmgAmt);
    }

    public static void damageEntityWeapon(double dmgAmt, LivingEntity recipient, Player caster) {

        // ignore NPCs
        if (recipient.hasMetadata("NPC")) return;
        if (recipient instanceof ArmorStand) return;

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(caster) != null
                && RunicCore.getPartyManager().getPlayerParty(caster).hasMember(recipient.getUniqueId())) { return; }

        dmgAmt = dmgAmt + GearScanner.getMagicBoost(caster);
        damageEntity(dmgAmt, recipient, caster);
        HologramUtil.createDamageHologram((caster), recipient.getLocation().add(0,1.5,0), dmgAmt);
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

        KnockbackUtil.knockback(caster, recipient);

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


