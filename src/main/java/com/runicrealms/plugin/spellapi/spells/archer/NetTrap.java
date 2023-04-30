package com.runicrealms.plugin.spellapi.spells.archer;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.Circle;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NetTrap extends Spell implements DurationSpell, RadiusSpell, WarmupSpell {
    private static final double PERCENT = .25;
    private final Set<UUID> weakenedMobs = new HashSet<>();
    private double trapDuration;
    private double warmup;
    private double radius;
    private double stunDuration;

    public NetTrap() {
        super("Net Trap", CharacterClass.ARCHER);
        this.setDescription("You lay down a trap, which arms after " + warmup +
                "s and lasts for " + trapDuration + "s. " +
                "The first enemy to step over the trap triggers it, " +
                "causing all enemies within " + radius + " " +
                "blocks to be lifted into the air and stunned for " +
                stunDuration + "s! Against mobs, you and your allies deal " +
                (100 + (PERCENT * 100)) + "% damage to them for the duration!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location location = player.getLocation();
        Location castLocation = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(), location.getBlockZ() + 0.5);
        Hologram hologram = HologramsAPI.createHologram(RunicCore.getInstance(), castLocation.getBlock().getLocation().add(0.5, 1.0, 0.5));
        hologram.appendItemLine(new ItemStack(Material.RABBIT_HIDE));
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
                        if (isValidEnemy(player, entity)) {
                            trapSprung = true;
                            springTrap((LivingEntity) entity);
                        }
                    }
                    if (trapSprung) {
                        this.cancel();
                        assert castLocation.getWorld() != null;
                        castLocation.getWorld().playSound(castLocation, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 1.0f);
                        castLocation.getWorld().playSound(castLocation, Sound.BLOCK_PORTAL_TRAVEL, 0.25f, 1.0f);
                        hologram.delete();
                        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                                () -> weakenedMobs.remove(player.getUniqueId()), (int) stunDuration * 20L);
                    }
                }

            }
        }.runTaskTimer(RunicCore.getInstance(), (long) (warmup * 20), 20L);
    }

    @Override
    public double getDuration() {
        return stunDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.stunDuration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number stunDuration = (Number) spellData.getOrDefault("stun-duration", 0);
        Number trapDuration = (Number) spellData.getOrDefault("trap-duration", 0);
        this.stunDuration = stunDuration.doubleValue();
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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!weakenedMobs.contains(event.getVictim().getUniqueId())) return;
        event.setAmount(event.getAmount() + event.getAmount());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!weakenedMobs.contains(event.getVictim().getUniqueId())) return;
        event.setAmount(event.getAmount() + event.getAmount());
    }

    private void springTrap(LivingEntity livingEntity) {
        Location higher = livingEntity.getLocation().add(0, 2, 0);
        livingEntity.getWorld().spawnParticle(Particle.CRIT, higher, 15, 0.25f, 0.25f, 0.25f, 0);
        livingEntity.teleport(higher);
        addStatusEffect(livingEntity, RunicStatusEffect.STUN, stunDuration, true);
        if (!(livingEntity instanceof Player)) {
            weakenedMobs.add(livingEntity.getUniqueId());
            // Mobs don't have a PlayerMoveEvent, so we keep teleporting them
            BukkitTask mobTeleportTask = Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(),
                    () -> livingEntity.teleport(higher), 0, 10L);
            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), mobTeleportTask::cancel, (int) stunDuration * 20L);
        }
    }
}
