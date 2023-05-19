package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class SilverBolt extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell, PhysicalDamageSpell {
    private static final double BEAM_WIDTH = 1.5;
    private double distance;
    private double duration;
    private double magicDamage;
    private double magicDamagePerLevel;
    private double physicalDamage;
    private double physicalDamagePerLevel;

    public SilverBolt() {
        super("Silver Bolt", CharacterClass.ROGUE);
        this.setDescription("You fire a silver bolt up to " + distance + " blocks away! " +
                "The first enemy hit by the bolt suffers " +
                "(" + physicalDamage + " + &f" + physicalDamagePerLevel + "x&7 lvl) physical⚔ " +
                "damage and is &7&obranded " +
                "&7for the next " + duration + "s. &7&oBranded &7enemies take an additional " +
                "(" + magicDamage + " + &f" + magicDamagePerLevel
                + "x&7 lvl) magicʔ damage from your basic attacks!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 0.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );

        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) distance).getLocation().add(0.5, 1, 0.5); // Center on block
            VectorUtil.drawLine(player, Particle.REDSTONE, Color.SILVER, player.getEyeLocation(), location, 0.5D, 1, 0.05f);
            spawnArrowTip(location, new Particle.DustOptions(Color.WHITE, 1), player, 1);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Particle.REDSTONE, Color.SILVER, player.getEyeLocation(), livingEntity.getEyeLocation(), 0.5D, 1, 0.05f);
            spawnArrowTip(livingEntity.getEyeLocation(), new Particle.DustOptions(Color.WHITE, 1), player, 1);
            DamageUtil.damageEntityPhysical(physicalDamage, livingEntity, player, false, true, this);
            Cone.coneEffect(livingEntity, Particle.REDSTONE, duration, 0, 20, Color.SILVER);
            // todo: add new 'branded' status effect, listener
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
    public double getMagicDamage() {
        return magicDamage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.magicDamage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return magicDamagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.magicDamagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getPhysicalDamage() {
        return physicalDamage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.physicalDamage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return physicalDamagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.physicalDamagePerLevel = physicalDamagePerLevel;
    }

    public void spawnArrowTip(Location center, Particle.DustOptions dustOptions, Player player, double size) {
        Vector[] directions = {
                new Vector(size, 0, size), // upper right direction
                new Vector(-size, 0, size), // upper left direction
                new Vector(0, 0, -size) // bottom direction
        };

        for (Vector direction : directions) {
            Location start = center.clone();
            Location end = center.clone().add(direction);
            spawnParticleLine(start, end, dustOptions, player);
        }
    }

    public void spawnParticleLine(Location start, Location end, Particle.DustOptions dustOptions, Player player) {
        // Get the vector from the start location to the end
        Vector vector = end.toVector().subtract(start.toVector());

        // Calculate the number of particles based on the distance between start and end
        int particles = (int) start.distance(end) * 10;

        // Spawn each particle along the line from start to end
        for (int i = 0; i <= particles; i++) {
            double progress = (double) i / particles;
            Location particleLocation = start.clone().add(vector.clone().multiply(progress));
            player.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 1, dustOptions);
        }
    }

}

