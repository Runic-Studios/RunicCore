package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Random;

public class DragonsBreath extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final int PERIOD = 1;
    private double damageAmt;
    private double duration;
    private double radius;
    private double damagePerLevel;

    public DragonsBreath() {
        super("Dragon's Breath", CharacterClass.MAGE);
        this.setDescription("Enemies in a " + radius + " block cone in front of you " +
                "suffer (" + damageAmt + " + &f" + damagePerLevel +
                "x&7 lvl) magicʔ damage per second for " + duration + "s! " +
                "Silences and stuns end this spell early.");
    }

    private void conjureBreath(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);
        // Visual effect
        Vector middle = player.getEyeLocation().getDirection().normalize();
        Vector one = rotateVectorAroundY(middle, -40);
        Vector two = rotateVectorAroundY(middle, -20);
        Vector three = rotateVectorAroundY(middle, 20);
        Vector four = rotateVectorAroundY(middle, 40);
        Vector[] vectors = new Vector[]{one, two, three, four};
        for (Vector vector : vectors) {
            spawnWaveFlameLine(player, vector, player.getEyeLocation());
        }
        // Damage entities in front of the player
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target))) {
            Location entityLocation = entity.getLocation();
            Vector directionToEntity = entityLocation.subtract(player.getLocation()).toVector().normalize();
            // Check if the entity is in front of the player (cosine of the angle between the vectors > 0)
            if (player.getLocation().getDirection().dot(directionToEntity) < 0) continue;
            DamageUtil.damageEntitySpell(damageAmt, (LivingEntity) entity, player, this);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > duration
                        || RunicCore.getStatusEffectAPI().hasStatusEffect(player.getUniqueId(), RunicStatusEffect.SILENCE)
                        || RunicCore.getStatusEffectAPI().hasStatusEffect(player.getUniqueId(), RunicStatusEffect.STUN)) {
                    this.cancel();
                } else {
                    count += PERIOD;
                    conjureBreath(player);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, PERIOD * 20L);
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
        return damageAmt;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damageAmt = magicDamage;
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

    public void spawnWaveFlameLine(Player player, Vector vector, Location location) {
        Vector look = vector.normalize();
        World world = player.getWorld();
        double distanceStep = 0.5;

        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(new Random(), 1);
        generator.setScale(0.5);

        for (double distance = 0.5; distance <= radius - 0.5; distance += distanceStep) {
            double yOffset = generator.noise(distance, 0, 0) * 0.5;
            Vector offset = new Vector(0, yOffset, 0);
            Vector particleDirection = look.clone().multiply(distance).add(offset);
            Location particleLocation = location.clone().add(particleDirection);
            world.spawnParticle(Particle.FLAME, particleLocation, 0, 0, 0, 0, 0);
        }
    }

}

