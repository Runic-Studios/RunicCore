package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Circle;
import com.runicrealms.plugin.utilities.DamageUtil;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SnareTrap extends Spell implements DurationSpell, RadiusSpell, WarmupSpell, PhysicalDamageSpell {
    private double trapDuration;
    private double warmup;
    private double radius;
    private double rootDuration;
    private double baseDamage;
    private double damagePerLevel;

    public SnareTrap() {
        super("Snare Trap", CharacterClass.ARCHER);
        this.setDescription("You lay down a trap, which arms after " + this.warmup +
                "s and lasts for " + this.trapDuration + "s. " +
                "The first enemy to step over the trap triggers it, " +
                "causing all enemies within " + this.radius + " " +
                "blocks to take (" + this.baseDamage + " + &f" +
                this.damagePerLevel + "x&7 " + " lvl) physical⚔ damage, " +
                "then become rooted for " + this.rootDuration + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location location = player.getLocation();
        Location castLocation = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);
        Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(castLocation.getBlock().getLocation().add(0.5, 1.0, 0.5));
        hologram.getLines().appendItem(new ItemStack(Material.RABBIT_HIDE));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > trapDuration) {
                    this.cancel();
                    hologram.delete();
                } else {
                    count += 1;
                    Circle.createParticleCircle(player, castLocation, (int) radius, Particle.CRIT);
                    boolean trapSprung = false;
                    for (Entity entity : player.getWorld().getNearbyEntities(castLocation, radius, radius, radius)) {
                        if (TargetUtil.isValidEnemy(player, entity)) {
                            trapSprung = true;
                            springTrap((LivingEntity) entity, player);
                        }
                    }
                    if (trapSprung) {
                        this.cancel();
                        assert castLocation.getWorld() != null;
                        castLocation.getWorld().playSound(castLocation, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 1.0f);
                        castLocation.getWorld().playSound(castLocation, Sound.BLOCK_PORTAL_TRAVEL, 0.25f, 1.0f);
                        hologram.delete();
                    }
                }

            }
        }.runTaskTimer(RunicCore.getInstance(), (long) (warmup * 20), 20L);
    }

    @Override
    public double getDuration() {
        return rootDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.rootDuration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number stunDuration = (Number) spellData.getOrDefault("stun-duration", 0);
        Number trapDuration = (Number) spellData.getOrDefault("trap-duration", 0);
        this.rootDuration = stunDuration.doubleValue();
        this.trapDuration = trapDuration.doubleValue();
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
    public double getWarmup() {
        return warmup;
    }

    @Override
    public void setWarmup(double warmup) {
        this.warmup = warmup;
    }

    @Override
    public double getPhysicalDamage() {
        return this.baseDamage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.baseDamage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    private void springTrap(@NotNull LivingEntity target, @NotNull Player caster) {
        Location location = target.getLocation().clone();
        target.getWorld().spawnParticle(Particle.CRIT, location, 15, 0.25f, 0.25f, 0.25f, 0);
        DamageUtil.damageEntityPhysical(this.baseDamage, target, caster, false, true, false, this);
        addStatusEffect(target, RunicStatusEffect.ROOT, rootDuration, true);
        if (!(target instanceof Player)) {
            // Mobs don't have a PlayerMoveEvent, so we keep teleporting them
            BukkitTask mobTeleportTask = Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(),
                    () -> target.teleport(location), 0, 10L);
            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), mobTeleportTask::cancel, (int) rootDuration * 20L);
        }
    }
}
