package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class MeteorShower extends Spell implements MagicDamageSpell, RadiusSpell, WarmupSpell {
    private static final int AMOUNT = 4;
    private static final int HEIGHT = 8;
    private static final int MAX_DIST = 12;
    private static final double FIREBALL_SPEED = 1.25D;
    private static final double RAY_SIZE = 1.0D;
    private double damage;
    private double radius;
    private double damagePerLevel;
    private double warmup;
    private LargeFireball meteor;

    public MeteorShower() {
        super("Meteor Shower", CharacterClass.MAGE);
        this.setDescription("You mark an area at your target " +
                "enemy or location within " + MAX_DIST + " blocks! " +
                "After " + warmup + "s, " +
                "four projectile meteors rain from the shower that deal " +
                "(" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to enemies within " + radius + " blocks on impact!");
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
        // Create shower after delay
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 1.0f);
        final Location[] trailLoc = {location.clone().add(0, HEIGHT, 0)};
        VectorUtil.drawLine(player, Particle.FLAME, Color.WHITE, trailLoc[0], location.clone().subtract(0, 20, 0), 2.5D, 5);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> summonMeteorShower(player, location), (long) warmup * 20L);
    }

    /**
     * @param player   who cast the spell
     * @param location to spawn explosion
     */
    private void explode(Player player, Location location) {
        new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.FLAME, location, Color.RED);
        player.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 10, 0.25f, 0, 0.25f, 0);
        for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidEnemy(player, target))) {
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, this);
            player.getWorld().playSound(location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
        }
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
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntity() instanceof LargeFireball) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFireballDamage(ProjectileHitEvent event) {
        if (!event.getEntity().equals(meteor)) return;
        Location location = meteor.getLocation();
        meteor.remove();
        event.setCancelled(true);
        Player player = (Player) meteor.getShooter();
        if (player == null) return;
        explode(player, location);
    }

    /**
     * @param player   who cast the spell
     * @param location to spawn the meteor
     */
    private void summonMeteorShower(Player player, Location location) {
        final Location[] meteorLocation = {location.clone().add(0, HEIGHT, 0)};
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= AMOUNT) {
                    this.cancel();
                } else {
                    count += 1;
                    final Vector velocity = new Vector(0, -1, 0).multiply(FIREBALL_SPEED);
                    meteor = (LargeFireball) player.getWorld().spawnEntity(meteorLocation[0].setDirection(velocity), EntityType.FIREBALL);
                    EntityTrail.entityTrail(meteor, Particle.FLAME);
                    meteor.setInvulnerable(true);
                    meteor.setIsIncendiary(false);
                    meteor.setYield(0F);
                    meteor.setShooter(player);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
                    // Repeatedly set velocity to prevent players redirecting meteor
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!meteor.isDead()) {
                                meteor.setVelocity(velocity);
                            } else {
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(RunicCore.getInstance(), 0L, 2L); // Every 2 ticks (1/10th of a second)
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 20L);
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

