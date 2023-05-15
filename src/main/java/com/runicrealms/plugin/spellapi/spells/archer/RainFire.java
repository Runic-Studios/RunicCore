package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.Random;

public class RainFire extends Spell implements DistanceSpell, PhysicalDamageSpell, RadiusSpell, WarmupSpell {
    private static final int HEIGHT = 8;
    private static final double BEAM_WIDTH = 1.5;
    private static final Random RANDOM = new Random();
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double radius;
    private double warmup;

    public RainFire() {
        super("Rain Fire", CharacterClass.ARCHER);
        this.setDescription("You mark a target enemy or location within " + distance + " blocks! " +
                "After a " + warmup + "s delay, a wave of arrows strikes " +
                "the area around the mark, dealing (" + damage + " + &f" + damagePerLevel +
                "x &7lvl) physical⚔ damage to all enemies within " + radius + " blocks of the impact!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );

        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, (int) distance).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
        } else {
            location = player.getTargetBlock(null, (int) distance).getLocation();
        }

        rainFire(player, location);
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
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
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

    public void playArrowFlurry(Player player, Location location, int arrowsCount) {
        if (location.getWorld() == null) {
            Bukkit.getLogger().warning("There was a problem getting Rain Fire world!");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            for (int i = 0; i < arrowsCount; i++) {
                Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                    double offsetX = (RANDOM.nextDouble() * 2 * radius) - radius;
                    double offsetZ = (RANDOM.nextDouble() * 2 * radius) - radius;

                    Location randomLocation = location.clone().add(offsetX, 0, offsetZ);

                    location.getWorld().playSound(randomLocation, Sound.ENTITY_ARROW_HIT, 2.0F, 1.0F);
                    final Location[] trailLoc = {randomLocation.clone().add(0, HEIGHT, 0)};
                    VectorUtil.drawLine(player, Particle.FLAME, Color.WHITE, trailLoc[0], randomLocation.clone().subtract(0, 20, 0), 2.0D, 5);
                });
                try {
                    Thread.sleep(100);  // 100 milliseconds pause between each sound
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void rainFire(Player player, Location location) {
        assert location.getWorld() != null;
        final Location[] trailLoc = {location.clone().add(0, HEIGHT, 0)};
        VectorUtil.drawLine(player, Particle.CRIT, Color.WHITE, trailLoc[0], location.clone().subtract(0, 20, 0), 2.0D, 5);
        new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.CRIT, location, Color.RED);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            playArrowFlurry(player, location, 6);
            new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.FLAME, location, Color.RED);
            for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidEnemy(player, target))) {
                DamageUtil.damageEntityPhysical(damage, (LivingEntity) entity, player, false, true, this);
            }
        }, (long) (warmup * 20L));
    }
}
