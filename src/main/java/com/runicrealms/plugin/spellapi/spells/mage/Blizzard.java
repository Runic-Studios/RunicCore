package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
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

public class Blizzard extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final int MAX_DIST = 10;
    private static final int SLOW_DURATION = 2;
    private static final double SNOWBALL_SPEED = 0.5;
    private static final double RAY_SIZE = 1.0D;
    private double radius;
    private double damagePerLevel;
    private double damage;
    private double duration;
    private Snowball snowball;

    public Blizzard() {
        super("Blizzard", CharacterClass.MAGE);
        this.setDescription("You summon a cloud of snow that " +
                "rains down snowballs for " + duration + " seconds, " +
                "each dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to enemies and slowing them!");
    }

    private void blizzardDamage(Player player, Location location) {
        new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.REDSTONE, location, Color.AQUA);
        for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidEnemy(player, target))) {
            player.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25F, 1.0F);
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, this);
            addStatusEffect((LivingEntity) entity, RunicStatusEffect.SLOW_III, SLOW_DURATION, false);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
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
        if (!event.getEntity().equals(this.snowball)) return;
        Location location = snowball.getLocation();
        snowball.remove();
        event.setCancelled(true);
        Player player = (Player) snowball.getShooter();
        if (player == null) return;
        blizzardDamage(player, location);
    }

    private void spawnBlizzard(Player player, Location location) {
        Vector launchPath = new Vector(0, -1.0, 0).multiply(SNOWBALL_SPEED);

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > duration)
                    this.cancel();
                else {
                    count++;

                    Location cloudLoc = new Location(player.getWorld(), location.getX(),
                            player.getLocation().getY(), location.getZ()).add(0, 7.5, 0);

                    // Sounds, reduced volume due to quantity of snowballs
                    player.getWorld().playSound(cloudLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.25f, 1.0f);
                    player.getWorld().spawnParticle(Particle.REDSTONE, cloudLoc,
                            25, 1.5f, 0.75f, 0.75f, new Particle.DustOptions(Color.WHITE, 20));

                    // Spawn 9 snowballs in a 3x3 square
                    snowball = spawnSnowball(player, cloudLoc, launchPath);
                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(-2, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(2, 0, 1), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, -2), launchPath);
                    spawnSnowball(player, cloudLoc.add(-1, 0, 2), launchPath);
                    spawnSnowball(player, cloudLoc.add(-1, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, -2), launchPath);
                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20); // drops a snowball every second
    }

    private Snowball spawnSnowball(Player player, Location loc, Vector vec) {
        Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
        snowball.setVelocity(vec);
        snowball.setShooter(player);
        return snowball;
    }
}

