package com.runicrealms.plugin.spellapi.spells.rogue;

import com.google.common.util.concurrent.AtomicDouble;
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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Whirlpool extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell, PhysicalDamageSpell, RadiusSpell {
    private static final double BEAM_WIDTH = 1.0D;
    private final Set<Location> whirlpools = new HashSet<>();
    private double distance;
    private double duration;
    private double magicDamage;
    private double magicDamagePerLevel;
    private double multiplier;
    private double physicalDamage;
    private double physicalDamagePerLevel;
    private double radius;

    public Whirlpool() {
        super("Whirlpool", CharacterClass.ROGUE);
        this.setDescription("You fire a stream of water ahead of you, " +
                "dealing (" + physicalDamage + " + &f" + physicalDamagePerLevel + "x&7 lvl) " +
                "physical⚔ damage to the first enemy hit. " +
                "If it lands, you summon a whirlpool at your enemy’s location, " +
                "pulling them in and dealing (" + magicDamage + " + &f" + magicDamagePerLevel + "x&7 lvl) " +
                "magicʔ damage per second, lasting " + this.duration + "s!");
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number multiplier = (Number) spellData.getOrDefault("multiplier", 0);
        this.multiplier = multiplier.doubleValue();
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
            summonWhirlPool(player, livingEntity);
        }
    }

    /**
     * Creates the whirlpool effect
     *
     * @param caster    who cast the spell
     * @param recipient of the harpoon
     */
    private void summonWhirlPool(Player caster, LivingEntity recipient) {
        Location castLocation = recipient.getLocation();
        whirlpoolEffect(caster, recipient, castLocation);
        AtomicDouble count = new AtomicDouble(1);
        whirlpools.add(castLocation);

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (count.get() > duration) {
                whirlpools.remove(castLocation);
                task.cancel();
                return;
            }

            if (count.get() % 1 == 0) {
                whirlpoolEffect(caster, recipient, castLocation);
            }

            for (Entity entity : recipient.getWorld().getNearbyEntities(castLocation, radius,
                    radius, radius, target -> isValidEnemy(caster, target))) {
                if (count.get() % 1 == 0) {
                    DamageUtil.damageEntitySpell(magicDamage, (LivingEntity) entity, caster, this);
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

            count.set(count.get() + 0.25);
        }, 0, 5);
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

    public boolean isInWhirlPool(@NotNull LivingEntity entity) {
        for (Location whirlpool : this.whirlpools) {
            if (!whirlpool.isWorldLoaded() || !entity.getWorld().equals(whirlpool.getWorld())) {
                continue;
            }

            if (entity.getLocation().distance(whirlpool) <= this.radius) {
                return true;
            }
        }

        return false;
    }
}
