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
import com.runicrealms.plugin.spellapi.spellutil.EntityUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * New cyromancer spell 1
 *
 * @author BoBoBalloon
 */
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
        this.setDescription("You deal " + this.damage + " magicʔ damage in a cone in front of you and slow enemies for " + this.duration + "s. \n" +
                "If enemies are slowed, deal " + this.slownessExtraDamage + " extra magicʔ damage and root them for " + (this.duration / 2) + "s. \n" +
                "If enemies are rooted, deal " + this.rootedExtraDamage + " extra magicʔ damage and stun them for " + (this.duration / 4) + "s. \n" +
                "If enemies are stunned, deal " + this.stunnedExtraDamage + " extra magicʔ damage. Enemies hit by the last effect are no longer stunned.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        this.drawCone(player);

        for (Entity target : EntityUtil.getEnemiesInCone(player, (int) this.radius, Frostbite.ANGLE, entity -> this.isValidEnemy(player, entity))) {
            if (!(target instanceof LivingEntity entity)) {
                continue;
            }

            double extraDamage = 0;
            if (this.hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.STUN)) {
                this.removeStatusEffect(entity, RunicStatusEffect.STUN);
                extraDamage = this.stunnedExtraDamage;
            } else if (this.hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.ROOT)) {
                this.removeStatusEffect(entity, RunicStatusEffect.ROOT);
                extraDamage = this.rootedExtraDamage;
            } else if (this.hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.SLOW_I) || this.hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.SLOW_II) || this.hasStatusEffect(entity.getUniqueId(), RunicStatusEffect.SLOW_III)) {
                this.removeStatusEffect(entity, RunicStatusEffect.SLOW_I);
                this.removeStatusEffect(entity, RunicStatusEffect.SLOW_II);
                this.removeStatusEffect(entity, RunicStatusEffect.SLOW_III);
                extraDamage = this.slownessExtraDamage;
            } else {
                this.addStatusEffect(entity, RunicStatusEffect.SLOW_I, this.duration, true);
            }

            DamageUtil.damageEntitySpell(this.damage + extraDamage, entity, player, false);

            if (extraDamage == this.rootedExtraDamage) {
                this.addStatusEffect(entity, RunicStatusEffect.STUN, this.duration / 4, true);
            } else if (extraDamage == this.slownessExtraDamage) {
                this.addStatusEffect(entity, RunicStatusEffect.ROOT, this.duration / 2, true);
            }
        }

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.5F, 1);
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

