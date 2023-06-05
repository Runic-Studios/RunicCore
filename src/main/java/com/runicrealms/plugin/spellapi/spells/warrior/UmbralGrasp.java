package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UmbralGrasp extends Spell implements DurationSpell, MagicDamageSpell {
    private final Map<UUID, WitherSkull> witherSkullMap = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double launchMultiplier;
    private double speedMultiplier;

    public UmbralGrasp() {
        super("Umbral Grasp", CharacterClass.WARRIOR);
        this.setDescription("You conjure a spectral skull and launch it forwards! " +
                "Hitting an enemy deals (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage on impact, " +
                "launches you backwards, teleports you directory to the target, then slows them for " + duration + "s " +
                "and deals the same damage again!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        witherSkullMap.put(player.getUniqueId(), player.launchProjectile(WitherSkull.class));
        WitherSkull witherSkull = witherSkullMap.get(player.getUniqueId());
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(speedMultiplier);
        witherSkull.setVelocity(velocity);
        witherSkull.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 0.5f);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGraspDamage(ProjectileHitEvent event) {
        if (witherSkullMap.isEmpty()) return;
        if (event.getEntity().getShooter() == null) return;
        if (!(event.getEntity().getShooter() instanceof Player player)) return;
        if (!witherSkullMap.containsKey(player.getUniqueId())) return;
        WitherSkull witherSkull = witherSkullMap.get(player.getUniqueId());
        witherSkull.remove();
        witherSkullMap.remove(player.getUniqueId());
        event.setCancelled(true);
        if (!(event.getHitEntity() instanceof LivingEntity victim)) return;
        if (!isValidEnemy(player, victim)) return;
        DamageUtil.damageEntitySpell(this.damage, victim, player, this);
        victim.getWorld().spawnParticle(Particle.SLIME, victim.getEyeLocation(), 3, 0.5F, 0.5F, 0.5F, 0);
        // Initial velocity
        Vector look = player.getLocation().getDirection();
        Vector launchPath = new Vector(-look.getX(), 1.5, -look.getZ()).normalize();
        player.setVelocity(launchPath.multiply(launchMultiplier));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(),
                10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(185, 251, 185), 5));
        // Teleport to target!
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            player.teleport(victim);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.2f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 0.75f);
            player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation(),
                    10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(185, 251, 185), 5));
            addStatusEffect(victim, RunicStatusEffect.SLOW_II, duration, false);
            DamageUtil.damageEntitySpell(this.damage, victim, player, this);
        }, 15L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof WitherSkull) {
            event.setDamage(0);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        // Check if the entity causing the explosion is a WitherSkull
        if (event.getEntityType() == EntityType.WITHER_SKULL) {
            // Clear the list of blocks to be affected by the explosion
            event.setCancelled(true);
            event.blockList().clear();
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

    public void setLaunchMultiplier(double launchMultiplier) {
        this.launchMultiplier = launchMultiplier;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number launchMultiplier = (Number) spellData.getOrDefault("launch-multiplier", 0);
        setLaunchMultiplier(launchMultiplier.doubleValue());
        Number speedMultiplier = (Number) spellData.getOrDefault("speed-multiplier", 0);
        setSpeedMultiplier(speedMultiplier.doubleValue());
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
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

}

