package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spells.warrior.Consecrate;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

public class Frostbite extends Spell implements DistanceSpell, DurationSpell, RadiusSpell {
    private static final double RAY_SIZE = 1.0D;
    private double distance; // 10
    private double duration;
    private double radius;

    public Frostbite() {
        super("Frostbite", CharacterClass.MAGE);
        this.setDescription("You conjure a ring of frost at " +
                "your target enemy or location up to " + distance +
                " blocks away for " + duration + "s, rooting all enemies caught " +
                "in the frost!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        RAY_SIZE,
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
        summonFrostbite(player, location);
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

    /**
     * @param player   who cast the spell
     * @param location to spawn the ring
     */
    private void summonFrostbite(Player player, Location location) {
        Block targetBlockLocation = location.getBlock();

        while (targetBlockLocation.getType() != Material.AIR)
            targetBlockLocation = targetBlockLocation.getRelative(BlockFace.UP);

        Location finalTargetBlockLocation = targetBlockLocation.getLocation();
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > duration) {
                    this.cancel();
                } else {
                    count += 1;
                    Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(),
                            () -> {
                                new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.CLOUD, finalTargetBlockLocation, 20);
                                Consecrate.createStarParticles(finalTargetBlockLocation, radius, Particle.BLOCK_CRACK, 6);
                            });
                    player.getWorld().playSound(finalTargetBlockLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
                    for (Entity entity : player.getWorld().getNearbyEntities(finalTargetBlockLocation, radius, radius, radius)) {
                        if (!(isValidEnemy(player, entity))) continue;
                        LivingEntity victim = (LivingEntity) entity;
                        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
                        addStatusEffect(victim, RunicStatusEffect.ROOT, (duration + 2) - count, true); // root for remaining duration
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }
}

