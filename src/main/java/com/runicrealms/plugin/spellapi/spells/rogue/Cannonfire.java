package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Cannonfire extends Spell implements DurationSpell, PhysicalDamageSpell {
    private static final int PELLET_SPEED = 2;
    private static final int TOTAL_PELLETS = 5;
    private static final Material MATERIAL = Material.FIREWORK_STAR;
    private final HashMap<UUID, Set<UUID>> hasBeenHit;
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

        Vector middle = player.getEyeLocation().add(0, 1, 0).getDirection().normalize().multiply(PELLET_SPEED);
        Vector left = rotateVectorAroundY(middle, -10);
        Vector right = rotateVectorAroundY(middle, 10);

        this.hasBeenHit.putIfAbsent(player.getUniqueId(), new HashSet<>());

        firePellets(player, middle, left, right);
    }

    private void explode(Entity victim, Player shooter, Spell spell, Entity pellet) {
        hasBeenHit.get(shooter.getUniqueId()).add(victim.getUniqueId()); // prevent concussive hits

        pellet.remove();
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 1.0f);
        victim.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, victim.getLocation(), 1, 0, 0, 0, 0);
        DamageUtil.damageEntityPhysical(damage, (LivingEntity) victim, shooter, false, false, spell);
        addStatusEffect((LivingEntity) victim, RunicStatusEffect.SLOW_II, duration, true);
        Vector force = shooter.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize().multiply(knockbackMultiplier);
        victim.setVelocity(force);
    }

    private void firePellets(Player player, Vector... vectors) {
        ItemStack item = new ItemStack(MATERIAL);

        for (Vector vector : vectors) {
            Item pellet = player.getWorld().dropItem(player.getEyeLocation(), item);
            pellet.setPickupDelay(Integer.MAX_VALUE);
            pellet.setVelocity(vector);

            Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
                if (pellet.isOnGround() || pellet.isDead()) {
                    pellet.remove();
                    task.cancel();
                    return;
                }

                Location loc = pellet.getLocation();
                pellet.getWorld().spawnParticle(Particle.CRIT, pellet.getLocation(), 1, 0, 0, 0, 0);

                for (Entity entity : pellet.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                    if (!isValidEnemy(player, entity) || hasBeenHit.get(player.getUniqueId()).contains(entity.getUniqueId())) {
                        continue;
                    }

                    explode(entity, player, this, pellet);
                    hasBeenHit.get(player.getUniqueId()).add(entity.getUniqueId());
                }
            }, 0, 1);

            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), hasBeenHit.get(player.getUniqueId())::clear, (int) duration * 20L);
        }
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.hasBeenHit.remove(event.getPlayer().getUniqueId());
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
