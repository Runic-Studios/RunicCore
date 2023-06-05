package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Map;
import java.util.Random;

public class Ruination extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double effectCooldown;
    private double requiredSouls;
    private double percent;
    private double radius;

    public Ruination() {
        super("Ruination", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("After claiming " + requiredSouls + " &f&osouls&7, " +
                "your next spell unleashes the spirits of your victims! " +
                "For the next " + duration + "s, the souls stream out of your body, " +
                "dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage per second " +
                "and lowering healing received by " +
                (percent * 100) + "% in a " + radius + " block cone in front of you! " +
                "Cannot occur more than once every " + effectCooldown + "s.");
    }

    public void setEffectCooldown(double effectCooldown) {
        this.effectCooldown = effectCooldown;
    }

    private void conjureNightfall(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_DEATH, 0.25f, 0.5f);

        // Visual effect
        double maxAngle = 45;
        Vector middle = player.getEyeLocation().getDirection().normalize();
        Vector one = rotateVectorAroundY(middle, -maxAngle);
        Vector two = rotateVectorAroundY(middle, -maxAngle / 2);
        Vector three = rotateVectorAroundY(middle, maxAngle / 2);
        Vector four = rotateVectorAroundY(middle, maxAngle);

        Vector[] vectors = new Vector[]{one, two, three, four};
        for (Vector vector : vectors) {
            soulWaveEffect(player, vector, player.getEyeLocation());
        }
        double maxAngleCos = Math.cos(Math.toRadians(maxAngle));
        // Damage entities in front of the player
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius)) {
            Location entityLocation = entity.getLocation();
            Vector directionToEntity = entityLocation.subtract(player.getLocation()).toVector().normalize();
            // Check if the entity is in front of the player (cosine of the angle between the vectors > 0)
            double dot = player.getLocation().getDirection().dot(directionToEntity);
            if (dot < maxAngleCos) continue;
            if (isValidEnemy(player, entity)) {
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1.0f);
                DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, false, this);
                // todo: healing debuff
            }
        }
    }

    public void soulWaveEffect(Player player, Vector vector, Location location) {
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
            world.spawnParticle(Particle.SLIME, particleLocation, 0, 0, 0, 0, 0);
            world.spawnParticle(Particle.BLOCK_CRACK, particleLocation, 0, 0, 0, 0, 0, Bukkit.createBlockData(Material.SLIME_BLOCK));
        }
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (SoulReaper.getReaperTaskMap().isEmpty()) return;
        if (!SoulReaper.getReaperTaskMap().containsKey(event.getCaster())) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (SoulReaper.getReaperTaskMap().get(event.getCaster()).getStacks().get() < requiredSouls) return;
        Player player = event.getCaster();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.15f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 0.25f);
        // Cancel the future task to reset souls
        SoulReaper.getReaperTaskMap().get(event.getCaster()).reset(0, () -> {
        });
        // Manually reset souls
        SoulReaper.cleanupTask(player);
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                } else {
                    count += 1;
                    conjureNightfall(player);
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number effectCooldown = (Number) spellData.getOrDefault("effect-cooldown", 0);
        setEffectCooldown(effectCooldown.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
        Number requiredSouls = (Number) spellData.getOrDefault("required-souls", 0);
        setRequiredSouls(requiredSouls.doubleValue());
    }

    public void setRequiredSouls(double requiredSouls) {
        this.requiredSouls = requiredSouls;
    }

    public void setPercent(double percent) {
        this.percent = percent;
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
