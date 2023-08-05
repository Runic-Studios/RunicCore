package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Frostbite extends Spell implements DistanceSpell, DurationSpell, RadiusSpell, MagicDamageSpell {
    private static final double ANGLE = Math.PI / 3; //60 degrees
    private double distance;
    private double duration;
    private double radius;
    private double damage;
    private double slownessExtraDamage;
    private double rootedExtraDamage;
    private double stunnedExtraDamage;

    public Frostbite() {
        super("Frostbite", CharacterClass.MAGE);
        this.setDescription("Deal " + this.damage + " magicʔ damage in a cone in front of you and slow enemies for " + this.duration + "s. " +
                "If enemies are slowed, deal " + this.slownessExtraDamage + " extra magicʔ damage and root them for " + (this.duration / 2) + "s. " +
                "If enemies are rooted, deal " + this.rootedExtraDamage + " extra magicʔ damage and stun them for " + (this.duration / 4) + "s. " +
                "If enemies are stunned, deal " + this.stunnedExtraDamage + " extra magicʔ damage.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        this.drawCone(player);

        for (Entity target : this.getEnemiesInCone(player)) {
            if (!(target instanceof LivingEntity entity)) {
                continue;
            }

            double extraDamage = 0;
            if (this.hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.STUN)) {
                this.removeStatusEffect(entity, RunicStatusEffect.STUN);
                extraDamage = this.stunnedExtraDamage;
            } else if (this.hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.ROOT)) {
                this.removeStatusEffect(entity, RunicStatusEffect.ROOT);
                this.addStatusEffect(entity, RunicStatusEffect.STUN, this.duration / 4, true);
                extraDamage = this.stunnedExtraDamage;
            } else if (this.hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.SLOW_I)) {
                this.removeStatusEffect(entity, RunicStatusEffect.SLOW_I);
                this.addStatusEffect(entity, RunicStatusEffect.ROOT, this.duration / 2, true);
                extraDamage = this.slownessExtraDamage;
            } else {
                this.addStatusEffect(entity, RunicStatusEffect.SLOW_I, this.duration, true);
            }

            DamageUtil.damageEntitySpell(this.damage + extraDamage, entity, player, false);
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
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getMagicDamage() {
        return this.damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return 0;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {

    }

    @Override
    public void loadMagicData(Map<String, Object> spellData) {
        Number magicDamage = (Number) spellData.getOrDefault("magic-damage", 0);
        setMagicDamage(magicDamage.doubleValue());
        Number slownessExtraDamage = (Number) spellData.getOrDefault("slowness-extra-damage", 0);
        this.slownessExtraDamage = slownessExtraDamage.doubleValue();
        Number rootedExtraDamage = (Number) spellData.getOrDefault("rooted-extra-damage", 0);
        this.rootedExtraDamage = rootedExtraDamage.doubleValue();
        Number stunnedExtraDamage = (Number) spellData.getOrDefault("stunned-extra-damage", 0);
        this.stunnedExtraDamage = stunnedExtraDamage.doubleValue();
    }

    /**
     * A method used to get the entities in a cone in front of the player
     * Credit to ChatGPT for the vector math and shit
     *
     * @param player the player
     * @return the entities in a cone in front of the player
     */
    @NotNull
    public List<Entity> getEnemiesInCone(@NotNull Player player) {
        List<Entity> entitiesInCone = new ArrayList<>();
        Vector direction = player.getLocation().getDirection();

        for (Entity entity : player.getNearbyEntities(this.radius, this.radius, this.radius)) {
            if (entity.equals(player) || !this.isValidEnemy(player, entity)) {
                continue;
            }

            Vector relative = entity.getLocation().subtract(player.getLocation()).toVector();

            // Checking line of sight
            boolean hasLineOfSight = true;
            for (Block block : player.getLineOfSight(null, (int) this.radius)) {
                if (block.getLocation().distance(entity.getLocation()) < 1.0) {
                    hasLineOfSight = false;
                    break;
                }
            }

            if (Math.acos(direction.dot(relative) / (direction.length() * relative.length())) <= Frostbite.ANGLE && hasLineOfSight) {
                entitiesInCone.add(entity);
            }
        }

        return entitiesInCone;
    }

    /**
     * A method to draw the particle effect async
     * Full credit to ChatGPT
     *
     * @param player the player to use as the orgin
     */
    public void drawCone(@NotNull Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        Vector direction = loc.getDirection().normalize();
        double dirAngle = Math.atan2(direction.getZ(), direction.getX());

        // Define a blue color for the REDSTONE particle
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.AQUA, 1);

        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            for (double r = 0.5; r < this.radius; r += 0.5) {
                for (double theta = -Frostbite.ANGLE; theta <= Frostbite.ANGLE; theta += Math.PI / 180) {
                    double x = direction.getX() + r * Math.cos(theta + dirAngle);
                    double z = direction.getZ() + r * Math.sin(theta + dirAngle);
                    loc.add(x, 0, z);
                    player.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dustOptions);
                    loc.subtract(x, 0, z);
                }
            }
        });
    }
}

