package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class RayOfLight extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final int HEIGHT = 8;
    private static final int MAX_DURATION = 4; // how long until the beam just ends
    private static final int TRAIL_SPEED = 2;
    private static final double BEAM_WIDTH = 1.0D;
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double knockback;
    private double maxDistance;
    private double radius;

    public RayOfLight() {
        super("Ray Of Light", CharacterClass.CLERIC);
        this.setDescription("You call forth a ray of light that falls " +
                "from the sky at your target enemy or location within " +
                "8 blocks! Enemies within " + radius + " blocks of the impact take (" +
                damage + " + &f" + damagePerLevel + "x&7 lvl) magicʔ damage are " +
                "knocked away, and are silenced for " + duration + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        maxDistance,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
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

        lightBlast(player, location);
    }

    private void explode(Player player, Location location) {
        player.getWorld().spigot().strikeLightningEffect(location, true);
        player.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        player.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.add(0, 1, 0), 15, 0.25f, 0, 0.25f, 0);
        for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidEnemy(player, target))) {
            // Knock away
            Vector force = player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-knockback).setY(0.3);
            entity.setVelocity(force);
            DamageUtil.damageEntitySpell(damage, ((LivingEntity) entity), player, this);
            addStatusEffect((LivingEntity) entity, RunicStatusEffect.SILENCE, duration, true);

        }
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getDamagePerLevel() {
        return damagePerLevel;
    }

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

    public double getKnockback() {
        return knockback;
    }

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

    public double getMaxDistance() {
        return maxDistance;
    }

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

    /**
     * Spawns a falling beam of light from the sky that explodes upon hitting the ground
     *
     * @param player   who cast the spell
     * @param location to end the trail
     */
    private void lightBlast(Player player, Location location) {

        final Location[] trailLoc = {location.clone().add(0, HEIGHT, 0)};
        VectorUtil.drawLine(player, Particle.VILLAGER_ANGRY, Color.WHITE, trailLoc[0], location.clone().subtract(0, 20, 0), 2.5D, 5);

        BukkitTask bukkitTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (trailLoc[0].clone().subtract(0, 2, 0).getBlock().getType() != Material.AIR) { // block is on ground
                    this.cancel();
                    Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> explode(player, trailLoc[0]));
                }

                // spawn trail
                player.getWorld().playSound(trailLoc[0], Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 2.0f);
                player.getWorld().playSound(trailLoc[0], Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
                player.getWorld().spawnParticle(Particle.SPELL_INSTANT, trailLoc[0], 25, 0.75f, 0.75f, 0.75f, 0);
                trailLoc[0] = trailLoc[0].subtract(0, TRAIL_SPEED, 0);
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 3L);

        // So the beam doesn't last forever
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), bukkitTask::cancel, MAX_DURATION * 20L);
    }

}

