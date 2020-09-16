package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Sprint extends Spell {

    // global variables
    private final boolean flame;
    private static final int DURATION = 5;
    private static final int SPEED_AMPLIFIER = 2;

    private static final int DAMAGE_AMT = 20;
    private static final double FLAME_SPEED = 1.2;
    private static final int RADIUS = 5;

    // constructor
    public Sprint() {
        super("Sprint",
                "For " + DURATION + " seconds, you gain a" +
                        "\nmassive boost of speed!",
                ChatColor.WHITE, ClassEnum.ROGUE, 10, 10);
        flame = false;
    }

    public Sprint(boolean flame) {
        super("Sprint",
                "For " + DURATION + " seconds, you gain a" +
                        "\nmassive boost of speed!",
                ChatColor.WHITE, ClassEnum.ROGUE, 10, 10);
        this.flame = flame;
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // apply effects
        if (flame) {
            pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
            final Vector velocity = pl.getLocation().getDirection().add(new Vector(0, 0.5, 0)).normalize().multiply(FLAME_SPEED);
            pl.setVelocity(velocity);
            for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                if (!verifyEnemy(pl, en)) continue;
                en.getWorld().spawnParticle(Particle.FLAME, ((LivingEntity) en).getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
                DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, pl, 100);
            }
        }
        pl.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION *20, SPEED_AMPLIFIER));
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, pl.getLocation(), Color.FUCHSIA);
        new HorizCircleFrame(1).playParticle(Particle.TOTEM, pl.getEyeLocation(), Color.FUCHSIA);
    }

    public static int getDamageAmt() {
        return DAMAGE_AMT;
    }

    public static int getRadius() {
        return RADIUS;
    }
}

