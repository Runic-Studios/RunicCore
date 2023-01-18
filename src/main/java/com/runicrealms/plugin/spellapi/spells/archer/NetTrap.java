package com.runicrealms.plugin.spellapi.spells.archer;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
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
import java.util.Set;
import java.util.UUID;

public class NetTrap extends Spell {
    private static final int DURATION = 12;
    private static final int RADIUS = 2;
    private static final int STUN_DURATION = 3;
    private static final double PERCENT = .25;
    private static final double WARMUP = 0.5; // seconds
    private final Set<UUID> weakenedMobs = new HashSet<>();

    public NetTrap() {
        super("Net Trap",
                "You lay down a trap, which arms after " + WARMUP +
                        "s and lasts for " + DURATION + "s. " +
                        "The first enemy to step over the trap triggers it, " +
                        "causing all enemies within " + RADIUS + " " +
                        "blocks to be lifted into the air and stunned for " +
                        STUN_DURATION + "s! Against mobs, you and your allies deal " +
                        (100 + (PERCENT * 100)) + "% damage to them for the duration!",
                ChatColor.WHITE, CharacterClass.ARCHER, 15, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getLocation();
        Hologram hologram = HologramsAPI.createHologram(RunicCore.getInstance(), castLocation.getBlock().getLocation().add(0.5, 1.0, 0.5));
        hologram.appendItemLine(new ItemStack(Material.RABBIT_HIDE));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    hologram.delete();
                } else {
                    count += 1;
                    Circle.createParticleCircle(player, castLocation, RADIUS, Particle.CRIT);
                    boolean trapSprung = false;
                    for (Entity entity : player.getWorld().getNearbyEntities(castLocation, RADIUS, RADIUS, RADIUS)) {
                        if (isValidEnemy(player, entity)) {
                            trapSprung = true;
                            springTrap((LivingEntity) entity);
                        }
                    }
                    if (trapSprung) {
                        this.cancel();
                        hologram.delete();
                        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                                () -> weakenedMobs.remove(player.getUniqueId()), STUN_DURATION * 20L);
                    }
                }

            }
        }.runTaskTimer(RunicCore.getInstance(), (long) (WARMUP * 20), 20L);
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
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 1.0f);
        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.25f, 1.0f);
        livingEntity.teleport(higher);
        addStatusEffect(livingEntity, RunicStatusEffect.STUN, STUN_DURATION);
        if (!(livingEntity instanceof Player)) {
            weakenedMobs.add(livingEntity.getUniqueId());
            livingEntity.setGlowing(true);
            // Mobs don't have a PlayerMoveEvent, so we keep teleporting them
            BukkitTask mobTeleportTask = Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(),
                    () -> livingEntity.teleport(higher), 0, 5L);
            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
                livingEntity.setGlowing(false);
                mobTeleportTask.cancel();
            }, STUN_DURATION * 20L);
        }
    }
}
