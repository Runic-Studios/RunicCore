package com.runicrealms.plugin.spellapi.spells.runic.passive;

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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class Insanity extends Spell {

    private static final int DURATION = 2;
    private static final int PERCENT = 10;
    private static final int NAUSEA_MULT = 2;

    public Insanity() {
        super ("Insanity",
                "Damaging an enemy has a " + (int) PERCENT + "% chance" +
                        "\nto cause nausea for " + DURATION + " second(s)!",
                ChatColor.WHITE, ClassEnum.RUNIC, 0, 0);
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

        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;

        // particles, sounds
        if (verifyEnemy(pl, en)) {
            LivingEntity victim = (LivingEntity) en;
            victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25f, 1.75f);
            victim.getWorld().spawnParticle(Particle.SPELL_WITCH, victim.getEyeLocation(),
                    5, 0.5F, 0.5F, 0.5F, 0);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, DURATION * 20, NAUSEA_MULT));
        }
    }
}

