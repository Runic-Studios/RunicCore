package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Surge extends Spell implements DurationSpell {
    private static final double DURATION_FALL = 2.5;
    private final Map<UUID, BukkitTask> surgeTasks = new HashMap<>();
    private double speedMultiplier;
    private double duration;

    public Surge() {
        super("Surge", CharacterClass.ARCHER);
        this.setDescription("You launch yourself forward! " +
                "When you land a &aStormborn&7 arrow, reduce this skill’s cooldown by " + this.duration + "s.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // Particle trail
        Set<Location> trailSpots = new HashSet<>();
        BukkitTask trailTask = new BukkitRunnable() {
            @Override
            public void run() {
                trailSpots.add(player.getLocation());
                for (Location location : trailSpots) {
                    player.getWorld().spawnParticle(Particle.REDSTONE, location,
                            1, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 71, 72), 3));
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 5L);

        // Fall damage immunity
        BukkitTask surgeTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> {
                    surgeTasks.remove(player.getUniqueId());
                    trailTask.cancel();
                }, (long) (DURATION_FALL * 20L));

        // Forward jump
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 1.0F);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 0.8f);
        Vector unitVector = new Vector(player.getLocation().getDirection().getX(), 0, player.getLocation().getDirection().getZ());
        unitVector = unitVector.normalize();
        player.setVelocity(unitVector.multiply(speedMultiplier));
        player.setVelocity(unitVector);
        surgeTasks.put(player.getUniqueId(), surgeTask);
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        Number speedMultiplier = (Number) spellData.getOrDefault("speed-multiplier", 0);
        setSpeedMultiplier(speedMultiplier.doubleValue());
        super.loadSpellSpecificData(spellData);
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * Disable fall damage for players who are surging
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(EnvironmentDamageEvent event) {
        if (!surgeTasks.containsKey(event.getVictim().getUniqueId())) return;
        if (event.getCause() == EnvironmentDamageEvent.DamageCauses.FALL_DAMAGE)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onStormbornArrowHit(Stormborn.ArrowHitEvent event) {
        Set<Spell> onCooldown = RunicCore.getSpellAPI().getSpellsOnCooldown(event.getCaster().getUniqueId());

        if (onCooldown == null || onCooldown.stream().noneMatch(spell -> spell.getName().equals(this.getName()))) {
            return;
        }

        RunicCore.getSpellAPI().reduceCooldown(event.getCaster(), this, this.duration);
    }
}

