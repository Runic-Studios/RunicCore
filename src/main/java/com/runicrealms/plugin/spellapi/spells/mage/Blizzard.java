package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Blizzard extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell, RadiusSpell, WarmupSpell {
    private static final int HEIGHT = 9;
    private static final int SLOW_DURATION = 2;
    private static final double SNOWBALL_SPEED = 0.5;
    private static final double RAY_SIZE = 1.0D;
    // Add a set for the blizzard snowballs
    private final Set<Snowball> blizzardSnowballs = new HashSet<>();
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double duration;
    private double radius;
    private double warmup;

    public Blizzard() {
        super("Blizzard", CharacterClass.MAGE);
        this.setDescription("You mark an area at your target " +
                "enemy or location within " + distance + " blocks! " +
                "After " + warmup + "s, you rain down snowballs for " + duration + "s, " +
                "dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to enemies in the area and slowing them!");
    }

    private void blizzardDamage(Player player, Location location) {
        for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidEnemy(player, target))) {
            player.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25F, 1.0F);
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, this);
            addStatusEffect((LivingEntity) entity, RunicStatusEffect.SLOW_III, SLOW_DURATION, false);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities(
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

        if (location.getWorld() == null) {
            Bukkit.getLogger().warning("There was a problem getting world for Blizzard!");
            return;
        }

        // Cast a ray downwards to get the ground location
        RayTraceResult groundRayTraceResult = location.getWorld().rayTraceBlocks(location, new Vector(0, -1, 0), distance);
        if (groundRayTraceResult != null && groundRayTraceResult.getHitBlock() != null) {
            location = groundRayTraceResult.getHitBlock().getLocation().add(0.5, 1, 0.5);
        }

        // Create blizzard after delay
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 1.0f);
        final Location[] trailLoc = {location.clone().add(0, HEIGHT, 0)};
        VectorUtil.drawLine(player, Particle.SNOWBALL, Color.WHITE, trailLoc[0], location.clone().subtract(0, 20, 0), 2.5D, 5);
        Location finalLocation = location;
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> spawnBlizzard(player, finalLocation), (long) warmup * 20L);
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

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSnowballDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSnowballHit(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Snowball snowball)) return;

        // Check if snowball was created by Blizzard spell
        if (blizzardSnowballs.contains(snowball)) {
            snowball.remove();
            blizzardSnowballs.remove(snowball); // Remove snowball from set
            event.setCancelled(true);
        }
    }

    private void spawnBlizzard(Player player, Location location) {
        Vector launchPath = new Vector(0, -1.0, 0).multiply(SNOWBALL_SPEED);
        Location cloudLoc = location.clone().add(0, HEIGHT, 0);

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > duration)
                    this.cancel();
                else {
                    count++;
                    new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.REDSTONE, location, Color.WHITE);
                    new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.SNOWBALL, location, Color.WHITE);
                    // Sounds, reduced volume due to quantity of snowballs
                    player.getWorld().playSound(cloudLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.25f, 1.0f);
                    player.getWorld().spawnParticle(Particle.REDSTONE, cloudLoc,
                            25, 1.5f, 0.75f, 0.75f, new Particle.DustOptions(Color.WHITE, 20));
                    // Visual effect
                    spawnSnowballs(player, cloudLoc, launchPath);
                    // Damage
                    blizzardDamage(player, location);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20); // drops a snowball every second
    }

    private void spawnSnowball(Player player, Location loc, Vector vec) {
        Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
        snowball.setVelocity(vec);
        snowball.setShooter(player);
        blizzardSnowballs.add(snowball); // Add snowball to set
    }

    private void spawnSnowballs(Player player, Location cloudLoc, Vector launchPath) {
        int numberOfSnowballs = 4;  // Number of snowballs to spawn

        double fixedRadius = radius - 1;
        for (int i = 0; i < numberOfSnowballs; i++) {
            // Generate random offsets within the given radius
            double offsetX = (Math.random() * (fixedRadius * 2)) - fixedRadius;
            double offsetY = (Math.random() * (fixedRadius * 2)) - fixedRadius;
            double offsetZ = (Math.random() * (fixedRadius * 2)) - fixedRadius;

            // Create a new location offset by the random amounts
            Location spawnLocation = cloudLoc.clone().add(offsetX, offsetY, offsetZ);

            // Spawn the snowball at the offset location
            spawnSnowball(player, spawnLocation, launchPath);
        }
    }

    @Override
    public double getWarmup() {
        return warmup;
    }

    @Override
    public void setWarmup(double warmup) {
        this.warmup = warmup;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }
}

