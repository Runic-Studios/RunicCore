package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.modeled.ModeledStandAnimated;
import com.runicrealms.plugin.spellapi.modeled.StandSlot;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * The type Ray of light.
 */
@SuppressWarnings("FieldCanBeLocal")
public class RayOfLight extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell, RadiusSpell, WarmupSpell {
    private static final int MODEL_DATA_BEAM = 2485;
    private static final int[] MODEL_DATA_BEAM_ARRAY = new int[]{
            MODEL_DATA_BEAM,
            2486,
            2487,
            2488,
            2489,
            2490,
            2491,
            2492,
            2493
    };
    private static final int MODEL_DATA_EXPLOSION = 2476;
    private static final int[] MODEL_DATA_EXPLOSION_ARRAY = new int[]{
            MODEL_DATA_BEAM,
            2477,
            2478,
            2479,
            2480,
            2481,
            2482,
            2483,
            2484
    };
    private static final double BEAM_WIDTH = 1.0D;
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double knockback;
    private double maxDistance;
    private double radius;
    private double warmup;

    /**
     * Instantiates a new Ray of light.
     */
    public RayOfLight() {
        super("Ray Of Light", CharacterClass.CLERIC);
        this.setDescription("You call forth a ray of light at your target " +
                "enemy or location within " + maxDistance + " blocks " +
                "that charges for " + warmup + "s, then explodes! " +
                "Enemies within " + radius + " blocks of the explosion take (" +
                damage + " + &f" + damagePerLevel + "x&7 lvl) magicʔ damage, then " +
                "are knocked away and silenced for " + duration + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        maxDistance,
                        BEAM_WIDTH,
                        entity -> TargetUtil.isValidEnemy(player, entity)
                );

        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, (int) maxDistance).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
        } else {
            location = player.getTargetBlock(null, (int) maxDistance).getLocation();
        }

        playEffect(player, location);
    }

    private void playEffect(Player player, Location location) {
        player.playSound(
                location,
                "samus.bard.symphony_laser",
                SoundCategory.AMBIENT,
                0.75f,
                1.0f
        );
        new ModeledStandAnimated(
                player,
                location,
                new Vector(0, 0, 0),
                MODEL_DATA_BEAM,
                this.duration,
                1.0,
                StandSlot.ARM,
                null,
                MODEL_DATA_BEAM_ARRAY
        );
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), task -> {
            player.playSound(
                    location,
                    "samus.bard.symphony_explosion",
                    SoundCategory.AMBIENT,
                    0.5f,
                    1.0f
            );
            new ModeledStandAnimated(
                    player,
                    location,
                    new Vector(0, 0, 0),
                    MODEL_DATA_EXPLOSION,
                    this.duration,
                    1.0,
                    StandSlot.ARM,
                    null,
                    MODEL_DATA_EXPLOSION_ARRAY
            );
            explode(player, location);
        }, (long) this.warmup * 20L);
    }

    private void explode(Player player, Location location) {
        player.getWorld().spigot().strikeLightningEffect(location, true);
        player.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        player.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.add(0, 1, 0), 15, 0.25f, 0, 0.25f, 0);
        for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> TargetUtil.isValidEnemy(player, target))) {
            // Knock away
            Vector force = player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-knockback).setY(0.3);
            entity.setVelocity(force);
            DamageUtil.damageEntitySpell(damage, ((LivingEntity) entity), player, this);
            addStatusEffect((LivingEntity) entity, RunicStatusEffect.SILENCE, duration, true);

        }
    }

    /**
     * Gets damage.
     *
     * @return the damage
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets damage.
     *
     * @param damage the damage
     */
    public void setDamage(double damage) {
        this.damage = damage;
    }

    /**
     * Gets damage per level.
     *
     * @return the damage per level
     */
    public double getDamagePerLevel() {
        return damagePerLevel;
    }

    /**
     * Sets damage per level.
     *
     * @param damagePerLevel the damage per level
     */
    public void setDamagePerLevel(double damagePerLevel) {
        this.damagePerLevel = damagePerLevel;
    }

    @Override
    public double getDistance() {
        return maxDistance;
    }

    @Override
    public void setDistance(double distance) {
        this.maxDistance = distance;
    }

    @Override
    public void loadDistanceData(Map<String, Object> spellData) {
        Number distance = (Number) spellData.getOrDefault("max-distance", 0);
        setDistance(distance.doubleValue());
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
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("silence-duration", 0);
        setDuration(duration.doubleValue());
    }

    /**
     * Gets knockback.
     *
     * @return the knockback
     */
    public double getKnockback() {
        return knockback;
    }

    /**
     * Sets knockback.
     *
     * @param knockback the knockback
     */
    public void setKnockback(double knockback) {
        this.knockback = knockback;
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    /**
     * Gets max distance.
     *
     * @return the max distance
     */
    public double getMaxDistance() {
        return maxDistance;
    }

    /**
     * Sets max distance.
     *
     * @param maxDistance the max distance
     */
    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getWarmup() {
        return warmup;
    }

    @Override
    public void setWarmup(double warmup) {
        this.warmup = warmup;
    }
}

