package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
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
                ChatColor.WHITE, CharacterClass.WARRIOR, 0, 0);
        this.setIsPassive(true);
    }

    private void applySilence(Player pl, Entity en) {

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (isValidEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WOLF_HOWL, 0.25f, 1.75f);
            pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                    new Particle.DustOptions(Color.BLACK, 1));
            addStatusEffect(victim, RunicStatusEffect.SILENCE, DURATION, true);
        }
    }

    @EventHandler
    public void onSilencingHit(PhysicalDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        applySilence(e.getPlayer(), e.getVictim());
    }

    @EventHandler
    public void onSilencingHit(MagicDamageEvent e) {
        if (!hasPassive(e.getPlayer().getUniqueId(), this.getName())) return;
        applySilence(e.getPlayer(), e.getVictim());
    }
}

