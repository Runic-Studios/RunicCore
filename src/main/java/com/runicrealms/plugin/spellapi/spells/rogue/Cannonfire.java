package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.plugin.utilities.FloatingItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Cannonfire extends Spell implements DurationSpell, PhysicalDamageSpell {
    private static final int PELLET_SPEED = 1;
    private static final int TOTAL_PELLETS = 5;
    private static final Material MATERIAL = Material.FIREWORK_STAR;
    private final HashMap<UUID, UUID> hasBeenHit;
    private double knockbackMultiplier; // -2.75
    private double damage;
    private double duration;
    private double damagePerLevel;

    public Cannonfire() {
        super("Cannonfire", CharacterClass.ROGUE);
        this.hasBeenHit = new HashMap<>();
        this.setDescription("You fire a flurry of " + TOTAL_PELLETS + " shrapnel fragments! " +
                "On hit, each fragment deals " +
                "(" + damage + " + &f" + damagePerLevel + "x&7 lvl) physical⚔ damage, " +
                "slows the target for " + duration + "s, and launches them back!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 0.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 2.0f);
        final Vector vector = player.getEyeLocation().add(0, 1, 0).getDirection().normalize().multiply(PELLET_SPEED);
        Location left = player.getEyeLocation().clone().add(1, 0, 0);
        Location middle = player.getEyeLocation();
        Location right = player.getEyeLocation().clone().add(-1, 0, 0);
        firePellets(player, new Location[]{left, middle, right}, vector);
    }

    private void explode(Entity victim, Player shooter, Spell spell, Entity pellet) {
        hasBeenHit.put(shooter.getUniqueId(), victim.getUniqueId()); // prevent concussive hits
        pellet.remove();
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 1.0f);
        victim.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, victim.getLocation(), 1, 0, 0, 0, 0);
        DamageUtil.damageEntityPhysical(damage, (LivingEntity) victim, shooter, false, false, spell);
        addStatusEffect((LivingEntity) victim, RunicStatusEffect.SLOW_II, duration, true);
        Vector force = shooter.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize().multiply(knockbackMultiplier);
        victim.setVelocity(force);
    }

    private void firePellets(Player player, Location[] pelletLocations, Vector vector) {
        for (Location location : pelletLocations) {
            Entity pellet = FloatingItemUtil.spawnFloatingItem(location, MATERIAL, 50, vector, 0);
            Spell spell = this;
            new BukkitRunnable() {
                @Override
                public void run() {

                    if (pellet.isOnGround() || pellet.isDead()) {
                        if (pellet.isOnGround()) {
                            pellet.remove();
                        }
                        this.cancel();
                        return;
                    }

                    Location loc = pellet.getLocation();
                    pellet.getWorld().spawnParticle(Particle.CRIT, pellet.getLocation(), 1, 0, 0, 0, 0);

                    for (Entity entity : pellet.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                        if (isValidEnemy(player, entity)) {
                            if (hasBeenHit.get(player.getUniqueId()) == entity.getUniqueId())
                                continue; // todo: broken, needs to be Map<UUID, List<UUID>> and add it
                            explode(entity, player, spell, pellet);
                        }
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0, 1L);

            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), hasBeenHit::clear, (int) duration * 20L);
        }
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

    @Override
    public void loadPhysicalData(Map<String, Object> spellData) {
        Number knockback = (Number) spellData.getOrDefault("knockback-multiplier", 0);
        setKnockbackMultiplier(-1 * knockback.doubleValue());
        Number physicalDamage = (Number) spellData.getOrDefault("physical-damage", 0);
        setPhysicalDamage(physicalDamage.doubleValue());
        Number physicalDamagePerLevel = (Number) spellData.getOrDefault("physical-damage-per-level", 0);
        setPhysicalDamagePerLevel(physicalDamagePerLevel.doubleValue());
    }

    public void setKnockbackMultiplier(double knockbackMultiplier) {
        this.knockbackMultiplier = knockbackMultiplier;
    }
}
