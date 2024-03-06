package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.modeled.ModeledStandAnimated;
import com.runicrealms.plugin.spellapi.modeled.StandSlot;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DragonsBreath extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final int MODEL_DATA = 2752;
    private static final int[] MODEL_DATA_ARRAY = new int[]{
            MODEL_DATA,
            2753,
            2754,
            2755,
            2756,
            2757,
            2758,
            2759,
            2760,
    };
    private static final double HITBOX_SCALE = 0.5;
    private static final int PERIOD = 1;
    private double damageAmt;
    private double duration;
    private double radius;
    private double damagePerLevel;

    public DragonsBreath() {
        super("Dragon's Breath", CharacterClass.MAGE);
        this.setDescription("You conjure a torrent of flame ahead of you " +
                "in a cone! Your fiery breath deals (" + damageAmt + " + &f" + damagePerLevel +
                "x&7 lvl) magicʔ damage every second for " + duration + "s to " +
                "enemies within " + radius + " blocks! " +
                "Silences and stuns end this spell early. " +
                "You are slowed for the duration of the effect.");
    }

    private void conjureBreath(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
        Location sourceLocation = player.getEyeLocation().subtract(0, 0.4f, 0);
        Vector direction = sourceLocation.getDirection();
        Vector forward = direction.multiply(3); // Adjust the multiplier to set the distance in front of the player
        Location spawnLocation = sourceLocation.add(forward);
        new ModeledStandAnimated(
                player,
                spawnLocation,
                new Vector(0, 0, 0),
                MODEL_DATA,
                4.0,
                HITBOX_SCALE,
                StandSlot.ARM,
                null,
                MODEL_DATA_ARRAY
        );
        // Only hit enemies in front of the caster
        double maxAngle = 45;
        double maxAngleCos = Math.cos(Math.toRadians(maxAngle));
        // Damage entities in front of the player
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> TargetUtil.isValidEnemy(player, target))) {
            Location entityLocation = entity.getLocation();
            Vector directionToEntity = entityLocation.subtract(player.getLocation()).toVector().normalize();
            // Check if the entity is in front of the player (cosine of the angle between the vectors > 0)
            double dot = player.getLocation().getDirection().dot(directionToEntity);
            if (dot < maxAngleCos) continue;
            DamageUtil.damageEntitySpell(damageAmt, (LivingEntity) entity, player, this);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        this.addStatusEffect(player, RunicStatusEffect.SLOW_I, this.duration, false);
        AtomicInteger count = new AtomicInteger(1);

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (count.get() > duration
                    || RunicCore.getStatusEffectAPI().hasStatusEffect(player.getUniqueId(), RunicStatusEffect.SILENCE)
                    || RunicCore.getStatusEffectAPI().hasStatusEffect(player.getUniqueId(), RunicStatusEffect.STUN)) {
                task.cancel();
                removeStatusEffect(player, RunicStatusEffect.SLOW_I);
            } else {
                count.set(count.get() + PERIOD);
                conjureBreath(player);
            }
        }, 0, PERIOD * 20);
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

    public void spawnWaveFlameLine(Player player, Vector vector, Location location, double effectiveRadius) {
        Vector look = vector.normalize();
        World world = player.getWorld();
        double distanceStep = 0.5;

        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(), 1);
        generator.setScale(0.5);

        for (double distance = 0.5; distance <= effectiveRadius - 0.5; distance += distanceStep) {
            double yOffset = generator.noise(distance, 0, 0) * 0.5;
            Vector offset = new Vector(0, yOffset, 0);
            Vector particleDirection = look.clone().multiply(distance).add(offset);
            Location particleLocation = location.clone().add(particleDirection);
            world.spawnParticle(Particle.FLAME, particleLocation, 0, 0, 0, 0, 0);
        }
    }

}

