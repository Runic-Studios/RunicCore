package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.armorstand.ModeledStand;
import com.runicrealms.plugin.spellapi.event.ModeledStandCollideEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Meteor extends Spell implements MagicDamageSpell, RadiusSpell {
    private static final int METEOR_MODEL_DATA = 2274;
    private static final int HEIGHT = 8;
    private static final int MAX_DIST = 12;
    private static final double HITBOX_SCALE = .01;
    private static final double METEOR_SPEED = 1.5D;
    private static final double RAY_SIZE = 1.0D;
    private double damage;
    private double radius;
    private double damagePerLevel;

    public Meteor() {
        super("Meteor", CharacterClass.MAGE);
        this.setDescription("You instantly drop a powerful meteor at your target " +
                "enemy or location within " + MAX_DIST + " blocks, dealing " +
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
                        entity -> TargetUtil.isValidEnemy(player, entity)
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
        summonMeteor(player, location);
    }

    /**
     * @param player   who cast the spell
     * @param location to spawn explosion
     */
    private void explode(Player player, Location location) {
        new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.FLAME, location, Color.RED);
        player.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 0.5F);
        player.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().playSound(location, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5F, 0.5F);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 20, 0.5f, 0.5, 0.5f, 0);
        for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> TargetUtil.isValidEnemy(player, target))) {
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireballHit(ModeledStandCollideEvent event) {
        if (event.getModeledStand().getCustomModelData() != METEOR_MODEL_DATA) return;
        Player player = event.getModeledStand().getPlayer();
        explode(player, event.getModeledStand().getArmorStand().getLocation());
    }

    private void summonMeteor(Player player, Location location) {
        final Location meteorLocation = location.clone().add(0, HEIGHT, 0);
        Vector vector = new Vector(0, -1, 0).multiply(METEOR_SPEED);
        ModeledStand meteor = new ModeledStand(
                player,
                meteorLocation,
                vector,
                METEOR_MODEL_DATA,
                4.0,
                HITBOX_SCALE,
                entity -> TargetUtil.isValidEnemy(player, entity)
        );
        EntityTrail.entityTrail(meteor.getArmorStand(), Particle.FLAME);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.01f);
    }
}

