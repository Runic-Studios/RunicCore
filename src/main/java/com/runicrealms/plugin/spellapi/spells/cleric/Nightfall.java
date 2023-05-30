package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Map;
import java.util.Random;

public class Nightfall extends Spell implements DurationSpell, RadiusSpell, WarmupSpell {
    private double duration;
    private double durationInvulnerable;
    private double knockupMultiplier;
    private double radius;
    private double warmup;

    public Nightfall() {
        super("Nightfall", CharacterClass.CLERIC);
        this.setDescription("You slow yourself for " + warmup + "s, then fire a cone of lunar magic, " +
                "launching enemies into the air and causing them to float " +
                "for the next " + duration + "s. " +
                "All enemies hit are purged of all buffs! " +
                "Allies hit become invulnerable for " + durationInvulnerable + "s and " +
                "are cleansed of debuffs.");
    }

    public void setDurationInvulnerable(double durationInvulnerable) {
        this.durationInvulnerable = durationInvulnerable;
    }

    public void setKnockupMultiplier(double knockupMultiplier) {
        this.knockupMultiplier = knockupMultiplier;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number durationInvulnerable = (Number) spellData.getOrDefault("duration-invulnerable", 0);
        setDurationInvulnerable(durationInvulnerable.doubleValue());
        Number knockupMultiplier = (Number) spellData.getOrDefault("knockup-multiplier", 0);
        setKnockupMultiplier(knockupMultiplier.doubleValue());
    }

    private void conjureNightfall(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.75f, 0.5f);
        // Visual effect
        double maxAngle = 45;

        Vector middle = player.getEyeLocation().getDirection().normalize();
        Vector one = rotateVectorAroundY(middle, -maxAngle);
        Vector two = rotateVectorAroundY(middle, -maxAngle / 2);
        Vector three = rotateVectorAroundY(middle, maxAngle / 2);
        Vector four = rotateVectorAroundY(middle, maxAngle);

        Vector[] vectors = new Vector[]{one, two, three, four};
        for (Vector vector : vectors) {
            spawnWaveFlameLine(player, vector, player.getEyeLocation());
        }
        double maxAngleCos = Math.cos(Math.toRadians(maxAngle));
        // Damage entities in front of the player
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
            Location entityLocation = entity.getLocation();
            Vector directionToEntity = entityLocation.subtract(player.getLocation()).toVector().normalize();
            // Check if the entity is in front of the player (cosine of the angle between the vectors > 0)
            double dot = player.getLocation().getDirection().dot(directionToEntity);
            if (dot < maxAngleCos) continue;
            if (isValidEnemy(player, entity)) {
                entity.setVelocity(new Vector(0, 1, 0).normalize().multiply(knockupMultiplier));
                ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (int) duration * 20, 2));
                // Purge // todo:
                RunicCore.getStatusEffectAPI().getStatusEffectValues(entity.getUniqueId()).forEach((runicStatusEffect, longDoublePair) -> {
                    if (runicStatusEffect.isBuff()) {
                        RunicCore.getStatusEffectAPI().removeStatusEffect(entity.getUniqueId(), runicStatusEffect);
                    }
                });
            } else if (isValidAlly(player, entity)) {
                addStatusEffect((LivingEntity) entity, RunicStatusEffect.INVULNERABILITY, durationInvulnerable, true);
                // Cleanse todo: make API method
                RunicCore.getStatusEffectAPI().getStatusEffectValues(entity.getUniqueId()).forEach((runicStatusEffect, longDoublePair) -> {
                    if (!runicStatusEffect.isBuff()) {
                        RunicCore.getStatusEffectAPI().removeStatusEffect(entity.getUniqueId(), runicStatusEffect);
                    }
                });
            }
        }
    }

    public void spawnWaveFlameLine(Player player, Vector vector, Location location) {
        Vector look = vector.normalize();
        World world = player.getWorld();
        double distanceStep = 0.5;

        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(), 1);
        generator.setScale(0.5);

        for (double distance = 0.5; distance <= radius - 0.5; distance += distanceStep) {
            double yOffset = generator.noise(distance, 0, 0) * 0.5;
            Vector offset = new Vector(0, yOffset, 0);
            Vector particleDirection = look.clone().multiply(distance).add(offset);
            Location particleLocation = location.clone().add(particleDirection);
            world.spawnParticle(Particle.REDSTONE, particleLocation, 0, 0, 0, 0, 0, new Particle.DustOptions(Color.BLACK, 1));
            world.spawnParticle(Particle.BLOCK_CRACK, particleLocation, 0, 0, 0, 0, 0, Bukkit.createBlockData(Material.LAPIS_BLOCK));
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        addStatusEffect(player, RunicStatusEffect.SLOW_III, warmup, false);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 1.0f);
        Cone.coneEffect(player, Particle.REDSTONE, warmup, 0, 20, Color.BLACK);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> conjureNightfall(player), (long) warmup * 20L);
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

