package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class RainFire extends Spell implements DistanceSpell, PhysicalDamageSpell, RadiusSpell, WarmupSpell {
    private static final int HEIGHT = 8;
    private static final double BEAM_WIDTH = 1.5;
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

    public static void playArrowFlurry(Location location, int arrowsCount) {
        if (location.getWorld() == null) {
            Bukkit.getLogger().warning("There was a problem getting Rain Fire world!");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            for (int i = 0; i < arrowsCount; i++) {
                Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> location.getWorld().playSound(location, Sound.ENTITY_ARROW_HIT, 2.0F, 1.0F));
                try {
                    Thread.sleep(100);  // 100 milliseconds pause between each sound
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
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

    private void rainFire(Player player, Location location) {
        final Location[] trailLoc = {location.clone().add(0, HEIGHT, 0)};
//        VectorUtil.drawLine(player, Particle.FLAME, Color.WHITE, location, trailLoc[0].clone().subtract(0, 20, 0), 1.0D, 5);
        VectorUtil.drawLine(player, Particle.CRIT, Color.WHITE, trailLoc[0], location.clone().subtract(0, 20, 0), 2.0D, 5);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            playArrowFlurry(location, 6);
        }, (long) (warmup * 20L));
    }
}
