package com.runicrealms.plugin.spellapi.spells.archer;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.Bisected;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SacredGrove extends Spell implements DurationSpell, HealingSpell, RadiusSpell, WarmupSpell {
    private static final Map<UUID, Location> GROVE_LOCATION_MAP = new HashMap<>();
    private double duration;
    private double healAmt;
    private double healingPerLevel;
    private double radius;
    private double warmup;

    public SacredGrove() {
        super("Sacred Grove", CharacterClass.ARCHER);
        this.setDescription("You place down an enchanted flower at your location; " +
                "it grows over the next " + warmup + "s. " +
                "Upon reaching maturity, the flower pops, " +
                "healing✦ your allies within " + radius + " blocks for (" +
                (int) healAmt + " + &f" + healingPerLevel + "x&7 lvl) health every second for " + duration + "s!");
    }

    public static Map<UUID, Location> getGroveLocationMap() {
        return GROVE_LOCATION_MAP;
    }

    private void createHealingRunnable(Player player, Location location) {
        Spell spell = this;
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    Bukkit.getPluginManager().callEvent(new GroveExpiryEvent(player));
                    this.cancel();
                    GROVE_LOCATION_MAP.remove(player.getUniqueId());
                } else {
                    count += 1;
                    new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.VILLAGER_HAPPY, location, 5, Color.GREEN);
                    for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidAlly(player, target))) {
                        Player playerEntity = (Player) entity;
                        healPlayer(player, playerEntity, healAmt, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // Center block
        Location location = player.getLocation().getBlock().getLocation();
        location.setX(location.getX() + 0.5);
        location.setZ(location.getZ() + 0.5);
        GROVE_LOCATION_MAP.put(player.getUniqueId(), location);
        spawnFlower(player, location);
        // Runnable to 'grow' seedling
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= warmup) {
                    this.cancel();
                    player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.0f);
                    player.getWorld().playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                    createHealingRunnable(player, location);
                } else {
                    count += 1;
                    player.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
                    new HorizontalCircleFrame((float) (radius - warmup + count), false).playParticle(player, Particle.CRIT, location, 10, Color.GREEN);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
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
    public double getHeal() {
        return healAmt;
    }

    @Override
    public void setHeal(double heal) {
        this.healAmt = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
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

    /**
     * Spawns a flower block with a task to remove it
     *
     * @param location to spawn the block
     */
    private void spawnFlower(Player player, Location location) {
        Material oldMaterial = location.getBlock().getType();
        location.getBlock().setType(Material.DANDELION, false);
        Hologram hologram = HologramsAPI.createHologram(RunicCore.getInstance(), location.getBlock().getLocation().add(0.5, 2.5, 0.5));
        hologram.appendTextLine(ChatColor.WHITE + player.getName() + "'s " + ChatColor.GRAY + "Seedling");
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            hologram.clearLines();
            hologram.appendTextLine(ChatColor.WHITE + player.getName() + "'s " + ChatColor.GREEN + "Wild Growth");
            location.getBlock().setType(Material.SUNFLOWER, false);
            Bisected blockData = (Bisected) location.getBlock().getBlockData();
            blockData.setHalf(Bisected.Half.BOTTOM);
            location.getBlock().setBlockData(blockData, false);
            blockData.setHalf(Bisected.Half.TOP);
            Location higher = location.clone().add(0, 1, 0);
            Material oldMaterialHigher = higher.getBlock().getType();
            higher.getBlock().setType(Material.SUNFLOWER, false);
            location.clone().add(0, 1, 0).getBlock().setBlockData(blockData, false);
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
                hologram.delete();
                location.getBlock().setType(oldMaterial);
                higher.getBlock().setType(oldMaterialHigher);
            }, (int) duration * 20L);
        }, (int) warmup * 20L);
    }

    /**
     * This custom event is called when Sacred Grove expires
     */
    public static class GroveExpiryEvent extends Event implements Cancellable {
        private static final HandlerList handlers = new HandlerList();
        private final Player caster;
        private boolean isCancelled;

        /**
         * @param caster player who cast heal spell
         */
        public GroveExpiryEvent(Player caster) {
            this.caster = caster;
            this.isCancelled = false;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }

        public Player getCaster() {
            return this.caster;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public HandlerList getHandlers() {
            return handlers;
        }

        @Override
        public boolean isCancelled() {
            return this.isCancelled;
        }

        @Override
        public void setCancelled(boolean arg0) {
            this.isCancelled = arg0;
        }
    }

}

