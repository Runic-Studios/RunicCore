package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Skill for Stormshot subclass
 *
 * @author BoBoBalloon
 */
public class Jolt extends Spell implements MagicDamageSpell, RadiusSpell, DurationSpell, DistanceSpell, WarmupSpell {
    private double damage;
    private double damagePerLevel;
    private double radius;
    private double duration;
    private double distance;
    private double damageInterval;
    private double warmup;

    public Jolt() {
        super("Jolt", CharacterClass.ARCHER);
        this.setDescription("You fire a bolt of lightning up to " + distance + " blocks away! " +
                "If the bolt hits an enemy, you summon a storm of lightning around the bolt " +
                "in a " + radius + " block radius, dealing (" + this.damage + " + &f" + this.damagePerLevel +
                "x&7 lvl) magicʔ damage to enemies every " + this.damageInterval + "s. " +
                "The storm lasts for " + this.duration + "s.");
    }

    @Override
    public void executeSpell(@NotNull Player player, @NotNull SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        this.distance,
                        1.75,
                        entity -> isValidEnemy(player, entity)
                );

        Location location = player.getTargetBlock(null, (int) distance).getLocation();
        boolean foundEnemy = false;
        if (rayTraceResult != null && rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            location = livingEntity.getLocation();
            foundEnemy = true;
        }

        if (location.getWorld() == null) return;
        location.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 2.0F);
        location.getWorld().playSound(location, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.2f);
        location.getWorld().strikeLightningEffect(location);

        if (foundEnemy) {
            conjureLightningStorm(player, location);
        }
    }

    private void conjureLightningStorm(Player player, Location location) {
        Spell spell = this;
        assert location.getWorld() != null;
        new BukkitRunnable() {
            double count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                } else {
                    count += damageInterval;
                    new HorizontalCircleFrame((float) radius / 2, false).playParticle(player, Particle.CRIT_MAGIC, location, 3);
                    new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.CRIT_MAGIC, location, 3);
                    for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidEnemy(player, target))) {
                        LivingEntity target = (LivingEntity) entity;
                        entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.25f, 2.0f);
                        DamageUtil.damageEntitySpell(damage, target, player, false, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) damageInterval * 20L);
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getMagicDamage() {
        return this.damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getDistance() {
        return this.distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public double getWarmup() {
        return this.warmup;
    }

    @Override
    public void setWarmup(double warmup) {
        this.warmup = warmup;
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number damageInterval = (Number) spellData.getOrDefault("damage-interval", this.duration / 2);
        this.damageInterval = damageInterval.doubleValue();
    }
}
