package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SuppressWarnings("FieldCanBeLocal")
public class Marksman extends Spell {

    private static final int AMOUNT = 10;
    private static final int DISTANCE = 10;

    public Marksman() {
        super ("Marksman",
                "Damaging an enemy from " + DISTANCE + "blocks" +
                        "\naway or farther deals " + AMOUNT + " additional spellʔ" +
                        "\ndamage!",
                ChatColor.WHITE, 12, 15);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onNauseaHit(SpellDamageEvent e) {
        applyNausea(e.getPlayer(), e.getEntity());
    }

    @EventHandler
    public void onNauseaHit(WeaponDamageEvent e) {
        applyNausea(e.getPlayer(), e.getEntity());
    }

    private void applyNausea(Player pl, Entity en) {

        if (getRunicPassive(pl) == null) return;
        if (!getRunicPassive(pl).equals(this)) return;

        int distance = (int) pl.getLocation().distance(en.getLocation());
        if (distance < DISTANCE) return;


        // particles, sounds
        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 0.25f, 2.0f);
            DamageUtil.damageEntitySpell(AMOUNT, victim, pl, false);
            victim.getWorld().spawnParticle(Particle.SMOKE_LARGE, victim.getEyeLocation(),
                    5, 0.5F, 0.5F, 0.5F, 0);
        }
    }
}

