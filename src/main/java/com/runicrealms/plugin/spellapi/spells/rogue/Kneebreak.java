package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.SpellDamageEvent;
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
public class Kneebreak extends Spell {

    private static final int DURATION = 2;
    private static final int PERCENT = 10;
    private static final int SLOW_MULT = 2;

    public Kneebreak() {
        super ("Kneebreak",
                "Damaging an enemy has a " + PERCENT + "% chance " +
                        "to slow them for " + DURATION + " second(s)!",
                ChatColor.WHITE, ClassEnum.RUNIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onKneebreakHit(SpellDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        applySlow(e.getPlayer(), e.getEntity());
    }

    @EventHandler
    public void onKneebreakHit(WeaponDamageEvent e) {
        if (!hasPassive(e.getPlayer(), this.getName())) return;
        applySlow(e.getPlayer(), e.getEntity());
    }

    private void applySlow(Player pl, Entity en) {

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25f, 1.75f);
            victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getLocation(),
                    25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 3));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, SLOW_MULT));
        }
    }
}

