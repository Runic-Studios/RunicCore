package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
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

public class Blizzard extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final int HEIGHT = 9;
    private static final int MAX_DIST = 10;
    private static final int SLOW_DURATION = 2;
    private static final double SNOWBALL_SPEED = 0.5;
    private static final double RAY_SIZE = 1.0D;
    private double radius;
    private double damagePerLevel;
    private double damage;
    private double duration;

    public Blizzard() {
        super("Blizzard", CharacterClass.MAGE);
        this.setDescription("You summon a cloud of snow that " +
                "rains down snowballs for " + duration + " seconds, " +
                "each dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to enemies and slowing them!");
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
                MAX_DIST,
                RAY_SIZE,
                entity -> isValidEnemy(player, entity)
        );

        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
        } else {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        }

        if (location.getWorld() == null) {
            Bukkit.getLogger().warning("There was a problem getting world for Blizzard!");
            return;
        }

        // Cast a ray downwards to get the ground location
        RayTraceResult groundRayTraceResult = location.getWorld().rayTraceBlocks(location, new Vector(0, -1, 0), MAX_DIST);
        if (groundRayTraceResult != null && groundRayTraceResult.getHitBlock() != null) {
            location = groundRayTraceResult.getHitBlock().getLocation().add(0.5, 1, 0.5);
        }

        spawnBlizzard(player, location);
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSnowballDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSnowballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        snowball.remove();
        event.setCancelled(true);
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

}

