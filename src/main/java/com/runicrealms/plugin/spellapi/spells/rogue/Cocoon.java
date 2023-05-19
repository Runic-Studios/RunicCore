package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class Cocoon extends Spell implements DistanceSpell, DurationSpell, PhysicalDamageSpell {
    private static final double BEAM_WIDTH = 1.0D;
    public double duration;
    private double damage;
    private double damagePerLevel;
    private double distance;

    public Cocoon() {
        super("Cocoon", CharacterClass.ROGUE);
        this.setDescription("You launch a short-range string of web " +
                "that deals (" + damage + " + &f" + damagePerLevel + "x&7 lvl) physical⚔ " +
                "damage and slows the first enemy hit within " + distance + " blocks " +
                "for " + duration + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );

        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) distance).getLocation();
            VectorUtil.drawLine(player, Material.COBWEB, player.getEyeLocation(),
                    location, 0.5D, 5, 0.05f);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Material.COBWEB, player.getEyeLocation(),
                    livingEntity.getLocation(), 0.5D, 5, 0.05f);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.25f, 2.0f);
            addStatusEffect(livingEntity, RunicStatusEffect.SLOW_III, duration, false);
            DamageUtil.damageEntityPhysical(damage, livingEntity, player, false, false, this);
        }
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
}

