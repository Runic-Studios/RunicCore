package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
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

import java.util.Map;

public class Whirlpool extends Spell implements DistanceSpell, DurationSpell,
        MagicDamageSpell, PhysicalDamageSpell, RadiusSpell, WarmupSpell {
    private static final double BEAM_WIDTH = 1.0D;
    private double distance;
    private double duration;
    private double magicDamage;
    private double magicDamagePerLevel;
    private double multiplier;
    private double physicalDamage;
    private double physicalDamagePerLevel;
    private double radius;
    private double warmup;

    public Whirlpool() {
        super("Whirlpool", CharacterClass.ROGUE);
        this.setDescription("You fire a stream of water ahead of you, " +
                "dealing (" + physicalDamage + " + &f" + physicalDamagePerLevel + "x&7 lvl) " +
                "physical⚔ damage to the first enemy hit. " +
                "If it lands, you summon a whirlpool at your enemy’s location, " +
                "pulling them in and dealing (" + magicDamage + " + &f" + magicDamagePerLevel + "x&7 lvl) " +
                "magicʔ damage per second, lasting 4s!");
    }

    @Override
    public void loadWarmupData(Map<String, Object> spellData) {
        Number multiplier = (Number) spellData.getOrDefault("multiplier", 0);
        setMultiplier(multiplier.doubleValue());
        Number warmup = (Number) spellData.getOrDefault("warmup", 0);
        setWarmup(warmup.doubleValue());
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.5f, 1.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );

        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) distance).getLocation();
            VectorUtil.drawLine(player, Particle.REDSTONE, Color.fromRGB(0, 102, 204), player.getEyeLocation(),
                    location, 0.5D, 5, 0.05f);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Particle.REDSTONE, Color.fromRGB(0, 102, 204), player.getEyeLocation(),
                    livingEntity.getLocation(), 0.5D, 5, 0.05f);
            DamageUtil.damageEntityPhysical(physicalDamage, livingEntity, player, false, false, this);
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                    () -> summonWhirlPool(player, livingEntity), (long) warmup * 20L);
        }
    }

    /**
     * Creates the whirlpool effect
     *
     * @param caster    who cast the spell
     * @param recipient of the harpoon
     */
    private void summonWhirlPool(Player caster, LivingEntity recipient) {
        Spell spell = this;
        Location castLocation = recipient.getLocation();
        whirlpoolEffect(caster, recipient, castLocation);
        new BukkitRunnable() {
            double count = 1;

            @Override
            public void run() {

//                count += 0.25;
                if (count > duration)
                    this.cancel();

                if (count % 1 == 0) {
                    whirlpoolEffect(caster, recipient, castLocation);
                }

                for (Entity entity : recipient.getWorld().getNearbyEntities(castLocation, radius,
                        radius, radius, target -> isValidEnemy(caster, target))) {
                    if (count % 1 == 0) {
                        DamageUtil.damageEntitySpell(magicDamage, (LivingEntity) entity, caster, spell);
                    }

                    // Pull to middle
                    Vector directionToMiddle = castLocation.clone().subtract(entity.getLocation()).toVector();
                    if (directionToMiddle.lengthSquared() > 0) { // Check if the vector is not zero
                        directionToMiddle.setY(0);
                        directionToMiddle.normalize().multiply(multiplier); // Adjust this value to change the strength of the pull
                        entity.setVelocity(directionToMiddle);
                    }

                    addStatusEffect((LivingEntity) entity, RunicStatusEffect.SLOW_II, 1, false);
                }
                count += 0.25;
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 5L);
    }

    private void whirlpoolEffect(Player caster, LivingEntity recipient, Location castLocation) {
        new HorizontalCircleFrame((float) radius, false).playParticle(caster, Particle.REDSTONE,
                castLocation, Color.fromRGB(0, 64, 128));
        new HorizontalCircleFrame((float) (radius - 1), false).playParticle(caster, Particle.REDSTONE,
                castLocation, Color.fromRGB(0, 89, 179));
        new HorizontalCircleFrame((float) (radius - 2), false).playParticle(caster, Particle.REDSTONE,
                castLocation, Color.fromRGB(0, 102, 204));
        recipient.getWorld().playSound(castLocation,
                Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.5f, 1.0f);
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
        return magicDamage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.magicDamage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return magicDamagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.magicDamagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getPhysicalDamage() {
        return physicalDamage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.physicalDamage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return physicalDamagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.physicalDamagePerLevel = physicalDamagePerLevel;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
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
