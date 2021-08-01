package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SuppressWarnings("FieldCanBeLocal")
public class Backstab extends Spell {

    private static final double PERCENT = .5;

    public Backstab() {
        super("Backstab",
                "Damaging an enemy from behind " +
                        "deals " + (int) (PERCENT * 100 + 100) + "% damage!",
                ChatColor.WHITE, ClassEnum.ROGUE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onDamage(SpellDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        e.setAmount(doBackstab(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    @EventHandler
    public void onDamage(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        e.setAmount(doBackstab(e.getPlayer(), e.getVictim(), e.getAmount()));
    }

    private int doBackstab(Player pl, Entity en, int originalAmt) {

        /*
        if the dot-product of both entitys' vectors is greater than 0 (positive),
        then they're facing the same direction and it's a backstab
         */
        if (!(pl.getLocation().getDirection().dot(en.getLocation().getDirection()) >= 0.0D)) return originalAmt;

        // execute skill effects
        if (verifyEnemy(pl, en)) {
            LivingEntity le = (LivingEntity) en;
            le.getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);
            le.getWorld().playSound(le.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.5f, 1.0f);
            return (int) (originalAmt + (originalAmt * PERCENT));
        }

        return originalAmt;
    }
}

