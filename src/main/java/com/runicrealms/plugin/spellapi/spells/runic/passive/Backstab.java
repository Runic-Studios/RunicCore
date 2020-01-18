package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SuppressWarnings("FieldCanBeLocal")
public class Backstab extends Spell {

    private static final int DAMAGE_AMT = 10;

    public Backstab() {
        super("Backstab",
                "Damaging an enemy from behind" +
                        "\ndeals " + DAMAGE_AMT  + " additional spellʔ damage!",
                ChatColor.WHITE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onDamage(WeaponDamageEvent e) {

        if (getRunicPassive(e.getPlayer()) == null) return;
        if (!getRunicPassive(e.getPlayer()).equals(this)) return;
        Player pl = e.getPlayer();

        /*
        if the dot-product of both entitys' vectors is greater than 0 (positive),
        then they're facing the same direction and it's a backstab
         */
        if (!(pl.getLocation().getDirection().dot(e.getEntity().getLocation().getDirection()) >= 0.0D)) return;

        // execute skill effects
        if (verifyEnemy(pl, e.getEntity())) {
            LivingEntity le = (LivingEntity) e.getEntity();
            DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, false);
            le.getWorld().spawnParticle(Particle.CRIT_MAGIC, le.getEyeLocation(), 25, 0.25, 0.25, 0.25, 0);
            le.getWorld().playSound(le.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.5f, 1.0f);
        }
    }
}

