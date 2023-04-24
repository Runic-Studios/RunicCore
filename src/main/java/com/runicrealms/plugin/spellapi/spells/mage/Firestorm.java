package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Random;

public class Firestorm extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    public static final Random random = new Random(System.nanoTime());
    private static final int MAX_DIST = 12;
    private static final int PERIOD = 1;
    private static final double RAY_SIZE = 1.0D;
    /*
     * Particles to display
     */
    public int particles = 50;
    private double damageAmt;
    private double duration;
    private double radius;
    private double damagePerLevel;

    public Firestorm() {
        super("Firestorm", CharacterClass.MAGE);
        this.setDescription("You launch a stream of flame that stops at your target enemy " +
                "or location within " + MAX_DIST + " blocks. " +
                "For " + duration + "s, the flame erupts into a terrible " +
                "firestorm, damaging enemies within " + radius + " blocks " +
                "every " + PERIOD + "s for (" + damageAmt + " + &f" + damagePerLevel +
                "x&7 lvl) magicʔ damage!");
    }

    private void createSphere(Player player, Location location) {
        for (int i = 0; i < particles; i++) {
            Vector vector = getRandomVector().multiply(radius);
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
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getMagicDamage() {
        return damageAmt;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damageAmt = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
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
                if (count > duration) {
                    this.cancel();
                } else {
                    count += PERIOD;
                    // particles
                    player.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.25f, 0.2f);
                    createSphere(player, location);

                    for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidEnemy(player, target))) {
                        DamageUtil.damageEntitySpell(damageAmt, (LivingEntity) entity, player, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, PERIOD * 20L);
    }
}

