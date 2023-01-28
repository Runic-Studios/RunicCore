package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Random;

public class FireStorm extends Spell implements MagicDamageSpell {
    public static final Random random = new Random(System.nanoTime());
    private static final int DAMAGE_AMT = 4;
    private static final int DURATION = 6;
    private static final int MAX_DIST = 12;
    private static final int PERIOD = 1;
    private static final int RADIUS = 5;
    private static final double DAMAGE_PER_LEVEL = 0.55;
    private static final double RAY_SIZE = 1.0D;
    /*
     * Particles to display
     */
    public int particles = 50;

    public FireStorm() {
        super("Firestorm",
                "You launch a stream of flame that stops at your target enemy " +
                        "or location within " + MAX_DIST + " blocks. " +
                        "For " + DURATION + "s, the flame erupts into a terrible " +
                        "firestorm, damaging enemies within " + RADIUS + " blocks " +
                        "every " + PERIOD + "s for (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) magicʔ damage!",
                ChatColor.WHITE, CharacterClass.MAGE, 12, 25);
    }

    public static int getDuration() {
        return DURATION;
    }

    private void createSphere(Player player, Location location) {
        for (int i = 0; i < particles; i++) {
            Vector vector = getRandomVector().multiply(RADIUS);
            location.add(vector);
            player.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.ORANGE, 1));
            location.subtract(vector);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        RAY_SIZE,
                        entity -> isValidEnemy(player, entity)
                );
        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
        } else {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        }
        summonFireStorm(player, location);
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

    /**
     * @param player   who cast the spell
     * @param location to summon the firestorm
     */
    private void summonFireStorm(Player player, Location location) {
        VectorUtil.drawLine(player, Particle.FLAME, Color.FUCHSIA, player.getEyeLocation(), location, 0.5D, 1, 0.25f);
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
                    player.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.25f, 0.2f);
                    createSphere(player, location);

                    for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
                        DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) entity, player, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, PERIOD * 20L);
    }
}

