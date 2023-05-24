package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class Surge extends Spell {
    private static final double DURATION_FALL = 2.5;
    private static final double DELAY = 0.75;
    private final Map<UUID, BukkitTask> surgeTasks = new HashMap<>();
    private double launchMultiplier;
    private double speedMultiplier;
    private double verticalPower;

    public Surge() {
        super("Surge", CharacterClass.ARCHER);
        this.setDescription("You launch yourself forward then upwards!");
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean attemptToExecute(Player player) {
        if (!player.isOnGround()) {
            player.sendMessage(ChatColor.RED + "You must be on the ground to cast " + this.getName() + "!");
            return false;
        }
        return true;
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

        // Delayed upward momentum
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
            // spell variables, vectors
            Location location = player.getLocation();
            Vector look = location.getDirection();
            Vector launchPath = new Vector(look.getX(), verticalPower, look.getZ()).normalize();

            // particles, sounds
            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> player.getWorld().spigot().strikeLightningEffect(player.getLocation(), true));
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 2.0F);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.2f);
            player.getWorld().spawnParticle(Particle.REDSTONE, location,
                    1, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(0, 71, 72), 3));

            player.setVelocity(launchPath.multiply(launchMultiplier));
        }, (long) (DELAY * 20L));
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        Number launchMultiplier = (Number) spellData.getOrDefault("launch-multiplier", 0);
        setLaunchMultiplier(launchMultiplier.doubleValue());
        Number speedMultiplier = (Number) spellData.getOrDefault("speed-multiplier", 0);
        setSpeedMultiplier(speedMultiplier.doubleValue());
        Number verticalPower = (Number) spellData.getOrDefault("vertical-power", 0);
        setVerticalPower(verticalPower.doubleValue());
    }

    public double getLaunchMultiplier() {
        return launchMultiplier;
    }

    public void setLaunchMultiplier(double launchMultiplier) {
        this.launchMultiplier = launchMultiplier;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public double getVerticalPower() {
        return verticalPower;
    }

    public void setVerticalPower(double verticalPower) {
        this.verticalPower = verticalPower;
    }

    /**
     * Disable fall damage for players who are surging
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(GenericDamageEvent event) {
        if (!surgeTasks.containsKey(event.getVictim().getUniqueId())) return;
        if (event.getCause() == GenericDamageEvent.DamageCauses.FALL_DAMAGE)
            event.setCancelled(true);
    }

}

