package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.ThreatUtil;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;

public class Smite extends Spell implements DistanceSpell, MagicDamageSpell, RadiusSpell {
    private static final double BEAM_WIDTH = 1.5;
    private double maxDistance;
    private double damage;
    private double damagePerLevel;
    private double knockback;
    private double radius;

    public Smite() {
        super("Smite", CharacterClass.WARRIOR);
        this.setDescription("You fire a beam of light, " +
                "dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to the first enemy hit and double damage to mobs within " + radius + "blocks!" +
                "and knocking away all enemies within " + radius + " blocks! " +
                "This spell also taunts monsters, causing them to attack you!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
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
            VectorUtil.drawLine(player, Particle.CLOUD, Color.WHITE, player.getEyeLocation(), location, 0.5D, 1, 0.25f);
            player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, location, 8, 0.5f, 0.5f, 0.5f, 0);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Particle.CLOUD, Color.WHITE, player.getEyeLocation(), livingEntity.getEyeLocation(), 0.75D, 1, 0.15f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 2.0f);
            livingEntity.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, livingEntity.getEyeLocation(), 8, 0.8f, 0.5f, 0.8f, 0);
            for (Entity entity : livingEntity.getWorld().getNearbyEntities(livingEntity.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target))) {
                // Calculate knockback direction
                Vector attackerPos = player.getLocation().toVector();
                Vector enemyPos = entity.getLocation().toVector();
                Vector knockbackDirection = enemyPos.subtract(attackerPos).normalize();
                // Apply knockback to enemy
                Vector knockbackVector = knockbackDirection.multiply(knockback);
                knockbackVector.setY(knockbackVector.getY() + 0.15);
                entity.setVelocity(entity.getVelocity().add(knockbackVector));

                if (entity instanceof LivingEntity target && !(entity instanceof Player)) {
                    DamageUtil.damageEntitySpell(2 * (damage + (player.getLevel() * this.damagePerLevel)), target, player);
                }
            }
            DamageUtil.damageEntitySpell(damage, livingEntity, player, this);
            ThreatUtil.generateThreat(player, livingEntity);
        }
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
        Number distance = (Number) spellData.getOrDefault("distance", 0);
        setDistance(distance.doubleValue());
        Number knockback = (Number) spellData.getOrDefault("knockback", 0);
        setKnockback(knockback.doubleValue());
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

    public void setKnockback(double knockback) {
        this.knockback = knockback;
    }

}

