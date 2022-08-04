package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class Subdue extends Spell {

    private static final int DURATION = 2;
    private static final double PERCENT = 10;

    public Subdue() {
        super("Subdue",
                "Damaging an enemy has a " + (int) PERCENT + "% chance " +
                        "to silence them for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.WARRIOR, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSilencingHit(SpellDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        applySilence(e.getPlayer(), e.getVictim());
    }

    @EventHandler
    public void onSilencingHit(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        applySilence(e.getPlayer(), e.getVictim());
    }

    private void applySilence(Player pl, Entity en) {

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WOLF_HOWL, 0.25f, 1.75f);
            pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                    new Particle.DustOptions(Color.BLACK, 1));
            addStatusEffect(victim, EffectEnum.SILENCE, DURATION);
        }
    }
}

