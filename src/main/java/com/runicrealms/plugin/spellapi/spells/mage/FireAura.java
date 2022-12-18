package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class FireAura extends Spell implements MagicDamageSpell {

    public static final Random random = new Random(System.nanoTime());
    private static final int DAMAGE_AMT = 4;
    private static final double DAMAGE_PER_LEVEL = 0.55;
    private static final int DURATION = 6;
    private static final int PERIOD = 1;
    private static final int RADIUS = 5;
    /*
     * Particles to display
     */
    public int particles = 50;

    public FireAura() {
        super("Fire Aura",
                "For " + DURATION + " seconds, you conjure a terrible " +
                        "firestorm, damaging enemies within " + RADIUS + " blocks " +
                        "every " + PERIOD + "s for (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) magicʔ damage!",
                ChatColor.WHITE, CharacterClass.MAGE, 12, 25);
    }

    public static int getDuration() {
        return DURATION;
    }

    private void createSphere(Player pl, Location loc) {
        for (int i = 0; i < particles; i++) {
            Vector vector = getRandomVector().multiply(RADIUS);
            loc.add(vector);
            pl.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.ORANGE, 1));
            loc.subtract(vector);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Spell spell = this;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {

                if (count > DURATION) {
                    this.cancel();
                } else {

                    count += PERIOD;

                    // particles
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.25f, 0.2f);
                    createSphere(player, player.getEyeLocation());

                    for (Entity en : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                        if (isValidEnemy(player, en))
                            DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, player, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, PERIOD * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    private Vector getRandomVector() {
        double x, y, z;
        x = random.nextDouble() * 2 - 1;
        y = random.nextDouble() * 2 - 1;
        z = random.nextDouble() * 2 - 1;
        return new Vector(x, y, z).normalize();
    }
}

