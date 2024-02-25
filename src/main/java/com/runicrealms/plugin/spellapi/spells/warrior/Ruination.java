package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.event.SpellCastEvent;
import com.runicrealms.plugin.spellapi.event.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Ruination extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private final Set<UUID> cooldownPlayers = new HashSet<>();
    private final Set<UUID> weakenedHealers = new HashSet<>();
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
        this.setDescription("After claiming " + requiredSouls + " &3souls&7, " +
                "your next spell unleashes the spirits of your victims! " +
                "For the next " + duration + "s, the souls stream out of your body, " +
                "dealing (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage per second, " +
                "as well as lowering enemy healing received by " +
                (percent * 100) + "% in a " + radius + " block cone in front of you! " +
                "Cannot occur more than once every " + effectCooldown + "s.");
    }

    public void setEffectCooldown(double effectCooldown) {
        this.effectCooldown = effectCooldown;
    }

    private void conjureNightfall(Player player) {
        cooldownPlayers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> cooldownPlayers.remove(player.getUniqueId()), (long) effectCooldown * 20L);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PHANTOM_DEATH, 0.35f, 0.5f);

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
            if (TargetUtil.isValidEnemy(player, entity)) {
                weakenedHealers.add(entity.getUniqueId());
                Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> weakenedHealers.remove(entity.getUniqueId()), (long) duration * 20L);
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1.0f);
                DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, false, this);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHeal(SpellHealEvent event) {
        if (event.isCancelled()) return;
        if (weakenedHealers.isEmpty()) return;
        if (!weakenedHealers.contains(event.getPlayer().getUniqueId())) return;
        double reduction = event.getAmount() * percent;
        event.setAmount((int) (event.getAmount() - reduction));
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
            world.spawnParticle(Particle.REDSTONE, particleLocation, 0, 0, 0, 0, 0, new Particle.DustOptions(Color.TEAL, 1));
            world.spawnParticle(Particle.BLOCK_CRACK, particleLocation, 0, 0, 0, 0, 0, Bukkit.createBlockData(Material.SLIME_BLOCK));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (SoulReaper.getReaperTaskMap().isEmpty()) return;
        if (!SoulReaper.getReaperTaskMap().containsKey(event.getCaster())) return;
        if (cooldownPlayers.contains(event.getCaster().getUniqueId())) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (SoulReaper.getReaperTaskMap().get(event.getCaster()).getStacks().get() < requiredSouls) return;
        Player player = event.getCaster();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.35f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 0.25f);
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
