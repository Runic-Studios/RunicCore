package com.runicrealms.plugin.spellapi.spells.runic.passive;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class Agility extends Spell {

    private static final int DURATION = 2;
    private static final int PERCENT = 10;
    private static final int SPEED_MULT = 1;

    public Agility() {
        super ("Agility",
                "Damaging an enemy has a " + (int) PERCENT + "% chance" +
                        "\nto grant you speed for " + DURATION + " second(s)!",
                ChatColor.WHITE, ClassEnum.RUNIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onSpeedyHit(SpellDamageEvent e) {
        getSpeed(e.getPlayer(), e.getEntity());
    }

    @EventHandler
    public void onSpeedyHit(WeaponDamageEvent e) {
        getSpeed(e.getPlayer(), e.getEntity());
    }

    private void getSpeed(Player pl, Entity en) {

        if (getRunicPassive(pl) == null) return;
        if (!getRunicPassive(pl).equals(this)) return;

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (verifyEnemy(pl, en)) {
            pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.8f);
            pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                    25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 3));
            pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION * 20, SPEED_MULT));
        }
    }
}

