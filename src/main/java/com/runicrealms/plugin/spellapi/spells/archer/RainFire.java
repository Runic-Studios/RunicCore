package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import org.bukkit.entity.Player;

public class RainFire extends Spell implements DistanceSpell, PhysicalDamageSpell, RadiusSpell, WarmupSpell {
    private static final double BEAM_WIDTH = 1.5;
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double radius;
    private double warmup;

    public RainFire() {
        super("Rain Fire", CharacterClass.ARCHER);
        this.setDescription("You mark a target enemy or location within 15 blocks! " +
                "After a 0.75s delay, a wave of arrows strikes " +
                "the area around the mark, dealing 30 + (3x lvl) physical damage " +
                "to all enemies within 3 blocks of the impact!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
//        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
//        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
//                (
//                        player.getLocation(),
//                        player.getLocation().getDirection(),
//                        distance,
//                        BEAM_WIDTH,
//                        entity -> isValidEnemy(player, entity)
//                );
//
//        if (rayTraceResult == null) {
//            Location location = player.getTargetBlock(null, MAX_DIST).getLocation();
//            VectorUtil.drawLine(player, Particle.CLOUD, Color.WHITE, player.getEyeLocation(), location, 0.5D, 1, 0.25f);
//            player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, location, 8, 0.5f, 0.5f, 0.5f, 0);
//        } else if (rayTraceResult.getHitEntity() != null) {
//            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
//            VectorUtil.drawLine(player, Particle.CLOUD, Color.WHITE, player.getEyeLocation(), livingEntity.getEyeLocation(), 0.75D, 1, 0.15f);
//            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 2.0f);
//            livingEntity.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, livingEntity.getEyeLocation(), 8, 0.8f, 0.5f, 0.8f, 0);
//            applyAtonement(player, livingEntity);
//            DamageUtil.damageEntitySpell(damage, livingEntity, player, this);
//        }
    }

    @Override
    public double getDistance() {
        return 0;
    }

    @Override
    public void setDistance(double distance) {

    }

    @Override
    public double getPhysicalDamage() {
        return 0;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {

    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return 0;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {

    }

    @Override
    public double getRadius() {
        return 0;
    }

    @Override
    public void setRadius(double radius) {

    }

    @Override
    public double getWarmup() {
        return 0;
    }

    @Override
    public void setWarmup(double warmup) {

    }
}
