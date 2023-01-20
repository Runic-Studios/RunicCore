package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Surge extends Spell implements MagicDamageSpell {
    private static final int DAMAGE = 30;
    private static final int DURATION = 3;
    private static final double DURATION_FALL = 3.0;
    private static final int RADIUS = 2;
    private static final double DAMAGE_PER_LEVEL = 1.5;
    private static final double DELAY = 0.75;
    private static final double LAUNCH_MULTIPLIER = 1.75;
    private static final double SPEED_MULTIPLIER = 3.0;
    private static final double VERTICAL_POWER = 0.5;
    private final Map<UUID, BukkitTask> surgeTasks = new HashMap<>();

    public Surge() {
        super("Surge",
                "You surge forward then upwards, leaving a trail " +
                        "of lightning behind you! Enemies who step in the trail " +
                        "take " + "(" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) magicʔ damage per second! " +
                        "The trail lasts for " + DURATION + "s.",
                ChatColor.WHITE, CharacterClass.ARCHER, 3, 0); // todo
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
        Spell spell = this;
        // Damage trail
        Set<Location> trailSpots = new HashSet<>();
        BukkitTask trailTask = new BukkitRunnable() {
            @Override
            public void run() {
                trailSpots.add(player.getLocation());

                for (Location location : trailSpots) {
                    player.getWorld().spawnParticle(Particle.REDSTONE, location,
                            25, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.fromRGB(14, 32, 50), 1));
                    for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
                        if (!isValidEnemy(player, entity)) continue;
                        DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) entity, player, spell);
                    }
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
        Vector directionVector = player.getLocation().getDirection();
        directionVector.normalize().multiply(SPEED_MULTIPLIER);
        player.setVelocity(directionVector);
        surgeTasks.put(player.getUniqueId(), surgeTask);

        // Delayed upward momentum
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
            // spell variables, vectors
            Location location = player.getLocation();
            Vector look = location.getDirection();
            Vector launchPath = new Vector(look.getX(), VERTICAL_POWER, look.getZ()).normalize();

            // particles, sounds
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0F, 2.0F);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.2f);
            player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                    25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));

            player.setVelocity(launchPath.multiply(LAUNCH_MULTIPLIER));
        }, (long) (DELAY * 20L));
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
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

