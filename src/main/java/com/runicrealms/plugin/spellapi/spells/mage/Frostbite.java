package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.ChilledEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
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

import java.util.Map;
import java.util.Optional;

/**
 * @author BoBoBalloon, Skyfallin
 */
public class Frostbite extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell {
    private static final double MAX_ANGLE_RADIANS = Math.toRadians(60);
    private static final double RADIUS = 4.0;
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double duration;
    private double empoweredDamage;
    private double empoweredDamagePerLevel;
    private double slowDuration;

    public Frostbite() {
        super("Frostbite", CharacterClass.MAGE);
        this.setDescription("You deal (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) " +
                "magicʔ damage in a cone " +
                "in front of you and apply &bchilled &7to enemies hit for " + duration + "s. " +
                "If enemies are already &bchilled&7, remove the effect, " +
                "then this spell instead deals (" + this.empoweredDamage + " + &f" + this.empoweredDamagePerLevel +
                "x&7 lvl) magicʔ damage and slows them for " + slowDuration + "s! " +
                "\n\n&2&lEFFECT &bChilled" +
                "\n&bChilled &7enemies are fraught with cold! " +
                "Some spells are empowered against chilled targets.");
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);

        Number empoweredDamage = (Number) spellData.getOrDefault("empowered-damage", 75);
        setEmpoweredDamage(empoweredDamage.doubleValue());

        Number empoweredDamagePerLevel = (Number) spellData.getOrDefault("empowered-damage-per-level", 3.0);
        setEmpoweredDamagePerLevel(empoweredDamagePerLevel.doubleValue());

        Number slowDuration = (Number) spellData.getOrDefault("slow-duration", 2.0);
        setSlowDuration(slowDuration.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        this.drawCone(player);

        double maxAngleCos = Math.cos(MAX_ANGLE_RADIANS);

        // Damage entities in front of the player
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            Location entityLocation = entity.getLocation();
            Vector directionToEntity = entityLocation.subtract(player.getLocation()).toVector().normalize();
            // Check if the entity is in front of the player (cosine of the angle between the vectors > 0)
            double dot = player.getLocation().getDirection().dot(directionToEntity);
            if (dot < maxAngleCos) continue;
            // Apply or consume chilled, deal damage
            Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(player.getUniqueId(), entity.getUniqueId(), SpellEffectType.CHILLED);
            if (spellEffectOpt.isPresent()) {
                ChilledEffect chilledEffect = (ChilledEffect) spellEffectOpt.get();
                chilledEffect.cancel();
                double damagePerLevel = this.empoweredDamagePerLevel * player.getLevel();
                DamageUtil.damageEntitySpell(this.empoweredDamage + damagePerLevel, (LivingEntity) entity, player); // No spell scaling to apply custom
            } else {
                ChilledEffect chilledEffect = new ChilledEffect(player, (LivingEntity) entity, this.duration);
                chilledEffect.initialize();
                DamageUtil.damageEntitySpell(this.damage, (LivingEntity) entity, player, this);
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
    public double getMagicDamage() {
        return this.damage;
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

    /**
     * A method to draw the particle effect async
     * Full credit to ChatGPT
     *
     * @param player the player to use as the origin
     */
    public void drawCone(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        Vector direction = loc.getDirection().normalize();
        double dirAngle = Math.atan2(direction.getZ(), direction.getX());

        // Define a blue color for the red stone particle
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.AQUA, 1);

        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            for (double r = 0.5; r < RADIUS; r += 0.5) {
                for (double theta = -MAX_ANGLE_RADIANS; theta <= MAX_ANGLE_RADIANS; theta += Math.PI / 180) {
                    double x = direction.getX() + r * Math.cos(theta + dirAngle);
                    double z = direction.getZ() + r * Math.sin(theta + dirAngle);
                    loc.add(x, 0, z);
                    player.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dustOptions);
                    loc.subtract(x, 0, z);
                }
            }
        });
    }

    public void setSlowDuration(double slowDuration) {
        this.slowDuration = slowDuration;
    }

    public void setEmpoweredDamage(double empoweredDamage) {
        this.empoweredDamage = empoweredDamage;
    }

    public void setEmpoweredDamagePerLevel(double empoweredDamagePerLevel) {
        this.empoweredDamagePerLevel = empoweredDamagePerLevel;
    }
}

