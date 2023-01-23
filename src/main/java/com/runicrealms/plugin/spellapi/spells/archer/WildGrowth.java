package com.runicrealms.plugin.spellapi.spells.archer;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class WildGrowth extends Spell implements HealingSpell {
    private static final int DURATION = 4;
    private static final int RADIUS = 8;
    private static final double HEAL_AMT = 40.0;
    private static final double HEALING_PER_LEVEL = 1.2;

    public WildGrowth() {
        super("Wild Growth",
                "You place down an enchanted flower at your location; " +
                        "it grows over the next " + DURATION + "s. " +
                        "Upon reaching maturity, the flower pops, " +
                        "healing✦ your allies within " + RADIUS + " blocks for (" +
                        (int) HEAL_AMT + " + &f" + HEALING_PER_LEVEL + "x&7 lvl) health over " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.ARCHER, 20, 30);
    }

    private void createHealingRunnable(Player player, Location location) {
        Spell spell = this;
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= DURATION) {
                    this.cancel();
                } else {
                    count += 1;
                    new HorizontalCircleFrame(RADIUS, false).playParticle(player, Particle.VILLAGER_HAPPY, location, Color.GREEN);
                    for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
                        if (!isValidAlly(player, entity)) continue;
                        Player playerEntity = (Player) entity;
                        HealUtil.healPlayer(HEAL_AMT / DURATION, playerEntity, player, false, spell);
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
                if (count >= DURATION) {
                    this.cancel();
                    player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.0f);
                    player.getWorld().playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                    createHealingRunnable(player, location);
                } else {
                    count += 1;
                    player.getWorld().playSound(location, Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.0f);
                    new HorizontalCircleFrame(RADIUS - DURATION + count, false).playParticle(player, Particle.CRIT, location, Color.GREEN);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
    }

    @Override
    public int getHeal() {
        return (int) HEAL_AMT;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
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
            }, DURATION * 20L);
        }, DURATION * 20L);
    }
}

