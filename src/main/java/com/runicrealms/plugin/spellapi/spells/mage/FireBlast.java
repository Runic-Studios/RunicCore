package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class FireBlast extends Spell implements MagicDamageSpell, RadiusSpell {
    private static final int MAX_DIST = 10;
    private static final double KNOCKUP_MULTIPLIER = 1.0;
    private static final double RAY_SIZE = 1.5D;
    private double damage;
    private double damagePerLevel;
    private double radius;

    public FireBlast() {
        super("Fire Blast", CharacterClass.MAGE);
        this.setDescription("You erupt a powerful blast of fire at " +
                "your target enemy or location that deals " +
                "(" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to enemies within " + radius + " blocks and " +
                "knocks them up!");
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
        fireBlast(player, location);
    }

    /**
     * Erupts a column of flame at the given location and knocks up all enemies in the radius
     *
     * @param player        who cast the spell
     * @param blastLocation to erupt the flame
     */
    private void fireBlast(Player player, Location blastLocation) {
        player.getWorld().playSound(blastLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.LAVA, blastLocation, 25, 0.3f, 0.3f, 0.3f, 0);

        for (Entity entity : player.getWorld().getNearbyEntities(blastLocation, radius, radius, radius, target -> isValidEnemy(player, target))) {
            LivingEntity livingEntity = (LivingEntity) entity;
            DamageUtil.damageEntitySpell(damage, livingEntity, player, this);
            entity.getWorld().spawnParticle(Particle.FLAME, livingEntity.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
            entity.setVelocity(new Vector(0, 1, 0).normalize().multiply(KNOCKUP_MULTIPLIER));
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
}

