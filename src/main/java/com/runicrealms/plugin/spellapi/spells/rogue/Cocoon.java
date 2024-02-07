package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
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

import java.util.Map;

public class Cocoon extends Spell implements AttributeSpell, DistanceSpell, DurationSpell, PhysicalDamageSpell {
    private static final double BEAM_WIDTH = 1.0D;
    private double baseValue;
    private double duration;
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double maxStacks;
    private double multiplier;
    private double stackDuration;
    private String statName;

    public Cocoon() {
        super("Cocoon", CharacterClass.ROGUE);
        this.setDescription("You launch a short-range string of web " +
                "that deals (" + damage + " + &f" + damagePerLevel + "x&7 lvl) physical⚔ " +
                "damage to the first enemy hit within " + distance + " blocks, " +
                "then slows them and applies one stack of &9sundered &7for " + duration + "s!" +
                "\n\n&2&lEFFECT &9Sundered" +
                "\n&9Sundered &7enemies suffer an additional " +
                "(" + baseValue + " + " + multiplier + "x DEX)% physical damage from all sources! " +
                "Can stack up to " + maxStacks + " times. Each stack expires after " + stackDuration + "s.");
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number maxStacks = (Number) spellData.getOrDefault("max-stacks", 3);
        setMaxStacks(maxStacks.doubleValue());
        Number stackDuration = (Number) spellData.getOrDefault("stack-duration", 4);
        setStackDuration(stackDuration.doubleValue());
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

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }

    public void setStackDuration(double stackDuration) {
        this.stackDuration = stackDuration;
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return statName;
    }

    @Override
    public void setStatName(String statName) {
        this.statName = statName;
    }
}

