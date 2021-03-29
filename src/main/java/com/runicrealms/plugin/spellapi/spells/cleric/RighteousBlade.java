package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class RighteousBlade extends Spell {

    private static final int BLIND_MULT = 2;
    private static final int DURATION = 2;
    private static final int RADIUS = 5;
    private static final double PERCENT = 10;

    public RighteousBlade() {
        super ("Righteous Blade",
                "Your melee weapon⚔ attacks have a " + (int) PERCENT + "% chance " +
                        "to heal yourself and allies within " + RADIUS + " blocks " +
                        "for " + DURATION + "s!",
                ChatColor.WHITE, ClassEnum.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onBlindingHit(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        applyBlind(e.getPlayer(), e.getEntity());
    }

    // todo: hi
    private void applyBlind(Player pl, Entity en) {

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WOLF_HOWL, 0.25f, 1.75f);
            pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                    new Particle.DustOptions(Color.BLACK, 1));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, DURATION * 20, BLIND_MULT));
        }
    }
}

