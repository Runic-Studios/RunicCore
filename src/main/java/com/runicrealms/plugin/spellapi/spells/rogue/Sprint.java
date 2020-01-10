package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("FieldCanBeLocal")
public class Sprint extends Spell {

    // global variables
    private static final int DURATION = 7;
    private static final int SPEED_AMPLIFIER = 2;

    // constructor
    public Sprint() {
        super("Sprint",
                "For " + DURATION + " seconds, you gain a" +
                        "\nmassive boost of speed!", ChatColor.WHITE,10, 10);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply effects
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION *20, SPEED_AMPLIFIER));
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, pl.getLocation(), Color.FUCHSIA);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, pl.getEyeLocation(), Color.FUCHSIA);
    }
}

