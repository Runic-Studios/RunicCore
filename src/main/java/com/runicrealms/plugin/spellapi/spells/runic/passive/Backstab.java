package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SuppressWarnings("FieldCanBeLocal")
public class Backstab extends Spell {

    private static final int DAMAGE_AMT = 10;

    public Backstab() {
        super("Backstab",
                "Damaging an enemy from behind" +
                        "\ndeals " + DAMAGE_AMT  + " additional damage!",
                ChatColor.WHITE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onDamage(SpellDamageEvent e) {
        e.setAmount(doBackstab(e.getPlayer(), e.getEntity(), e.getAmount()));
    }

    @EventHandler
    public void onDamage(WeaponDamageEvent e) {
        e.setAmount(doBackstab(e.getPlayer(), e.getEntity(), e.getAmount()));
    }

    private int doBackstab(Player pl, Entity en, int originalAmt) {

        if (getRunicPassive(pl) == null) return originalAmt;
        if (!getRunicPassive(pl).equals(this)) return originalAmt;

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
            return originalAmt+DAMAGE_AMT;
        }

        return originalAmt;
    }
}

