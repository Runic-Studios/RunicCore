package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class Predator extends Spell {

    private static final int DURATION = 1;
    private static final int PERCENT = 25;
    private static final int SLOW_MULT = 2;

    public Predator() {
        super ("Predator",
                "Damaging an enemy has a " + PERCENT + "% chance" +
                        "\nto blind them for " + DURATION + " second(s)!",
                ChatColor.WHITE, 12, 15);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onBlindingHit(SpellDamageEvent e) {
        applyBlind(e.getPlayer(), e.getEntity());
    }

    @EventHandler
    public void onBlindingHit(WeaponDamageEvent e) {
        applyBlind(e.getPlayer(), e.getEntity());
    }

    private void applyBlind(Player pl, Entity en) {

        if (getRunicPassive(pl) == null) return;
        if (!getRunicPassive(pl).equals(this)) return;

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WOLF_HOWL, 0.25f, 1.75f);
            pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                    new Particle.DustOptions(Color.BLACK, 1));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, DURATION * 20, SLOW_MULT));
        }
    }
}

