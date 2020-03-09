package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

@SuppressWarnings("FieldCanBeLocal")
public class Sandstorm extends Spell {

    private static final int DURATION = 6;
    private static final int DAMAGE_AMT = 3;
    private static final int PERIOD = 1;
    private static final int RADIUS = 3;

    public Sandstorm() {
        super ("Sandstorm",
                "For " + DURATION + " seconds, you conjure a terrible" +
                        "\nsandstorm, damaging enemies within " + RADIUS + " blocks" +
                        "\n" + "every " + PERIOD + " second(s) for " + DAMAGE_AMT + " spellʔ damage" +
                        "\nand slowing them!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: 50%",
                ChatColor.WHITE, ClassEnum.MAGE, 10, 12);
    }

    // spell execute code
    @Override
    public void executeSpell(Player player, SpellItemType type) {

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
                        if (verifyEnemy(player, en)) {
                            DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, player, true);
                            ((LivingEntity) en).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, 0));
                        }
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, PERIOD*20L);
    }

    /**
     * Particles to display
     */
    public int particles = 50;

    private void createSphere(Player pl, Location loc) {
        for (int i = 0; i < particles; i++) {
            Vector vector = getRandomVector().multiply(RADIUS);
            loc.add(vector);
            pl.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(210, 180, 140), 1));
            loc.subtract(vector);
        }
    }

    public static final Random random = new Random(System.nanoTime());

    private Vector getRandomVector() {
        double x, y, z;
        x = random.nextDouble() * 2 - 1;
        y = random.nextDouble() * 2 - 1;
        z = random.nextDouble() * 2 - 1;
        return new Vector(x, y, z).normalize();
    }
}

