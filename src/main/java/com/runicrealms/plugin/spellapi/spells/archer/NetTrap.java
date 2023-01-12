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
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class NetTrap extends Spell {

    private static final int DURATION = 12;
    private static final int RADIUS = 2;
    private static final int STUN_DURATION = 3;
    private static final double WARMUP = 0.5; // seconds
    private final Map<UUID, Set<UUID>> mobsMap = new HashMap<>();

    public NetTrap() {
        super("Net Trap",
                "You lay down a trap, which arms after " + WARMUP +
                        "s and lasts for " + DURATION +
                        "s. The first enemy to step over the trap triggers it, " +
                        "causing all players within " + RADIUS +
                        " blocks to be lifted into the air and stunned for " +
                        STUN_DURATION + "s! Against mobs, you instead deal " + ChatColor.BOLD + ChatColor.GRAY + "double " + ChatColor.GRAY +
                        "damage to them for the duration!",
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
                            springTrap(player, (LivingEntity) entity);
                        }
                    }
                    if (trapSprung) {
                        this.cancel();
                        hologram.delete();
                        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                                () -> mobsMap.remove(player.getUniqueId()), STUN_DURATION * 20L);
                    }
                }

            }
        }.runTaskTimer(RunicCore.getInstance(), (long) (WARMUP * 20), 20L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!mobsMap.containsKey(event.getPlayer().getUniqueId())) return;
        if (!mobsMap.get(event.getPlayer().getUniqueId()).contains(event.getVictim().getUniqueId())) return;
        event.setAmount(event.getAmount() + event.getAmount());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!mobsMap.containsKey(event.getPlayer().getUniqueId())) return;
        if (!mobsMap.get(event.getPlayer().getUniqueId()).contains(event.getVictim().getUniqueId())) return;
        event.setAmount(event.getAmount() + event.getAmount());
    }

    private void springTrap(Player caster, LivingEntity livingEntity) {
        if (livingEntity instanceof Player) {
            Player player = (Player) livingEntity;
            Location higher = player.getLocation().add(0, 2, 0);
            player.getWorld().spawnParticle(Particle.CRIT, higher, 15, 0.25f, 0.25f, 0.25f, 0);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.25f, 1.0f);
            player.teleport(higher);
            addStatusEffect(livingEntity, RunicStatusEffect.STUN, STUN_DURATION);
        } else {
            caster.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 1.0f);
            caster.getWorld().playSound(livingEntity.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.25f, 1.0f);
            mobsMap.computeIfAbsent(caster.getUniqueId(), k -> new HashSet<>());
            mobsMap.get(caster.getUniqueId()).add(livingEntity.getUniqueId());
            Cone.coneEffect(livingEntity, Particle.FIREWORKS_SPARK, STUN_DURATION, 0, 30L, Color.WHITE);
        }
    }
}
