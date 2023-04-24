package com.runicrealms.plugin.spellapi.spells.archer;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SacredGrove extends Spell implements DurationSpell, HealingSpell, RadiusSpell, WarmupSpell {
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

    private void createHealingRunnable(Player player, Location location) {
        Spell spell = this;
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                } else {
                    count += 1;
                    new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.VILLAGER_HAPPY, location, Color.GREEN);
                    for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                        if (!isValidAlly(player, entity)) continue;
                        Player playerEntity = (Player) entity;
                        healPlayer(player, playerEntity, healAmt, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location location = player.getLocation();
        spawnFlower(player, location);
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                    player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.0f);
                    player.getWorld().playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                    createHealingRunnable(player, location);
                } else {
                    count += 1;
                    player.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
                    new HorizontalCircleFrame((float) (radius - duration + count), false).playParticle(player, Particle.CRIT, location, Color.GREEN);
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
        }, (int) duration * 20L);
    }
}

