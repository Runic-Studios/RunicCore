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
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Skill for Stormshot subclass
 *
 * @author BoBoBalloon
 */
public class Jolt extends Spell implements MagicDamageSpell, RadiusSpell, DurationSpell, DistanceSpell, WarmupSpell {
    private static final long REFRESH = 20;
    private double damage;
    private double damagePerLevel;
    private double radius;
    private double duration;
    private double distance;
    private double damageInterval;
    private double dissipateDuration;
    private double warmup;

    public Jolt() {
        super("Jolt", CharacterClass.ARCHER);
        this.setDescription("You fire a bolt of lightning, on hit it leaves electricity in a " + this.radius + " block radius for " + this.dissipateDuration + "s. " +
                "If it hits an enemy, it deals (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) magicʔ damage to enemies every " + this.damageInterval + "s. " +
                "The storm lasts for " + this.duration + "s.");
    }

    @Override
    public void executeSpell(@NotNull Player player, @NotNull SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        this.distance,
                        1,
                        entity -> this.isValidEnemy(player, entity)
                );
        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, (int) this.distance).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
        } else {
            location = player.getTargetBlock(null, (int) this.distance).getLocation();
        }

        VectorUtil.drawLine(player, Particle.CRIT_MAGIC, Color.WHITE, player.getEyeLocation(), location, 2.5, 5);

        HorizontalCircleFrame stormOutline = new HorizontalCircleFrame((float) this.radius, false); //runs particles async

        long start = System.currentTimeMillis();
        AtomicBoolean dealtDamage = new AtomicBoolean(false);
        AtomicLong lastTimeDamaged = new AtomicLong((long) this.warmup * 1000);

        //start storm task
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (System.currentTimeMillis() >= (this.duration * 1000) + start ||
                    (!dealtDamage.get() && System.currentTimeMillis() >= (this.dissipateDuration * 1000) + start)) { //seconds to milliseconds
                task.cancel();
                return;
            }

            stormOutline.playParticle(player, Particle.CRIT_MAGIC, location, Color.BLUE); //CLOUD

            if (lastTimeDamaged.get() + (this.damageInterval * 1000) > System.currentTimeMillis()) {
                return;
            }

            lastTimeDamaged.set(System.currentTimeMillis());

            World realm = location.getWorld();

            if (realm == null) {
                return;
            }

            for (Entity entity : realm.getNearbyEntities(location, this.radius, this.radius, this.radius, entity -> entity instanceof LivingEntity && this.isValidEnemy(player, entity))) {
                LivingEntity target = (LivingEntity) entity;

                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 2.0F);
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.2f);
                target.getWorld().strikeLightningEffect(target.getLocation());

                DamageUtil.damageEntitySpell(this.damage, target, player, false, this);

                dealtDamage.set(true);
            }
        }, 0, REFRESH);
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
        Number dissipateDuration = (Number) spellData.getOrDefault("dissipate-duration", this.duration / 1.5);
        this.dissipateDuration = dissipateDuration.doubleValue();
    }
}
