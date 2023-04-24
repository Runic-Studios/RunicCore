package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
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
import org.bukkit.util.Vector;

import java.util.*;

public class UmbralGrasp extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell {
    private static final int RADIUS = 1;
    private static final double BEAM_WIDTH = 1.5;
    private static final double PERIOD = 0.5;
    private final Map<UUID, Set<UUID>> damageMap = new HashMap<>();
    private double maxDistance;
    private double damage;
    private double damagePerLevel;
    private double duration;

    public UmbralGrasp() {
        super("Umbral Grasp", CharacterClass.CLERIC);
        this.setDescription("You summon a wave of darkness at your target enemy or location within " + maxDistance + " " +
                "blocks that travels backward. Enemies hit by the wave suffer (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage, are pulled towards you, and are slowed for " + duration + "s!");
    }

    private void beginSpell(Player player, Location location, Location castLocation) {
        grasp(player, location);
        new BukkitRunnable() {
            double count = 1;

            @Override
            public void run() {
                if (count > maxDistance) {
                    this.cancel();
                    damageMap.remove(player.getUniqueId());
                } else {
                    count += 1 * PERIOD;
                    location.subtract(castLocation.getDirection());
                    grasp(player, location);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) PERIOD * 20L);
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
        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) maxDistance).getLocation();
            beginSpell(player, location.setDirection(player.getEyeLocation().getDirection()),
                    player.getEyeLocation());
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            beginSpell(player,
                    livingEntity.getEyeLocation().setDirection(player.getEyeLocation().getDirection()),
                    player.getEyeLocation());
        }
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public Map<UUID, Set<UUID>> getDamageMap() {
        return damageMap;
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

    private void grasp(Player player, Location location) {
        if (!damageMap.containsKey(player.getUniqueId()))
            damageMap.put(player.getUniqueId(), new HashSet<>());
        new HorizontalCircleFrame(RADIUS, true).playParticle(player, Particle.ASH, location);
        player.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 0.25f, 0.25f);
        for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            if (damageMap.get(player.getUniqueId()).contains(entity.getUniqueId())) continue;
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, this);
            pullTarget(((LivingEntity) entity), player.getLocation(), entity.getLocation());
            addStatusEffect((LivingEntity) entity, RunicStatusEffect.SLOW_III, duration, false);
            damageMap.get(player.getUniqueId()).add(entity.getUniqueId());
        }
    }

    /**
     * @param target         who was hit by the spell
     * @param casterLocation location of the caster
     * @param targetLocation location of the target
     */
    private void pullTarget(LivingEntity target, Location casterLocation, Location targetLocation) {
        org.bukkit.util.Vector pushUpVector = new org.bukkit.util.Vector(0.0D, 0.4D, 0.0D);
        target.setVelocity(pushUpVector);
        final double xDir = (casterLocation.getX() - targetLocation.getX()) / 3.0D;
        double zDir = (casterLocation.getZ() - targetLocation.getZ()) / 3.0D;
        org.bukkit.util.Vector pushVector = new Vector(xDir, 0.0D, zDir).normalize().multiply(2).setY(0.4D);
        target.setVelocity(pushVector);
    }


}

