package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.lootchests.BossChest;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@CommandAlias("runicboss")
public class RunicBossCMD extends BaseCommand {

    private static final int CHEST_DURATION = 10; // seconds (30)

    public RunicBossCMD() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("dungeons", context -> {
            Set<String> dungeons = new HashSet<>();
            for (DungeonLocation dungeonLocation : DungeonLocation.values()) {
                dungeons.add(dungeonLocation.getIdentifier());
            }
            return dungeons;
        });
    }

    @Default
    @CommandCompletion("@nothing @dungeons")
    @Conditions("is-console-or-op")
    public void onBaseCommand(CommandSender commandSender, String[] args) {
        if (args.length != 2) {
            commandSender.sendMessage(ChatColor.RED + "Error, correct usage: /runicboss [bossUuid] [dungeonName]");
            return;
        }
        UUID uuid = UUID.fromString(args[0]);
        DungeonLocation dungeonLocation = DungeonLocation.getFromIdentifier(args[1]);
        if (dungeonLocation == null) {
            Bukkit.getServer().getLogger().info(ChatColor.DARK_RED + "Error loading dungeon boss drop!");
            return;
        }
        spawnChest(uuid, dungeonLocation);
        // todo: fill with drops (same inventory click logic as loot chests) (weighted bag)
        // todo: only players who contributed to boss can open
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(),
                () -> despawnChest(uuid, dungeonLocation.getChestLocation()), CHEST_DURATION * 20L);
    }

    /**
     * @param uuid
     * @param dungeonLocation
     */
    private void spawnChest(UUID uuid, DungeonLocation dungeonLocation) {
        Location chestLocation = dungeonLocation.getChestLocation();
        assert chestLocation.getWorld() != null;
        Block block = chestLocation.getBlock();
        block.setType(Material.CHEST);
        org.bukkit.block.data.type.Chest blockData = (org.bukkit.block.data.type.Chest) block.getBlockData();
        blockData.setFacing(dungeonLocation.getChestBlockFace());
        BlockState state = block.getState();
        state.setBlockData(blockData);
        state.update();
        chestLocation.getWorld().playSound(chestLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        chestLocation.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, chestLocation, 25, 0.5f, 0.5f, 0.5f, 0);
        BossChest bossChest = new BossChest(uuid, (Chest) state, dungeonLocation);
        RunicCore.getBossTagger().getActiveBossLootChests().put(uuid, bossChest);
        spawnHologram(dungeonLocation);
    }

    /**
     * @param dungeonLocation
     */
    private void spawnHologram(DungeonLocation dungeonLocation) {
        Location location = dungeonLocation.getChestLocation().clone().add(0, 2, 0);
        Hologram hologram = HologramsAPI.createHologram(RunicCore.getInstance(), location);
        hologram.appendTextLine(ChatColor.GOLD + "" + ChatColor.BOLD + dungeonLocation.getDisplay() + " Spoils");
        hologram.appendTextLine(ChatColor.WHITE + "" + CHEST_DURATION + ChatColor.GRAY + " second(s) remaining");
        AtomicInteger count = new AtomicInteger(CHEST_DURATION);
        int hologramTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(RunicCore.getInstance(), () -> {
            count.getAndDecrement();
            ((TextLine) hologram.getLine(1)).setText(ChatColor.WHITE + "" + count + ChatColor.GRAY + " second(s) remaining");
        }, 20L, 20L);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            Bukkit.getScheduler().cancelTask(hologramTask);
            hologram.delete();
        }, CHEST_DURATION * 20L);
    }

    /**
     * @param uuid
     * @param chestLocation
     */
    private void despawnChest(UUID uuid, Location chestLocation) {
        assert chestLocation.getWorld() != null;
        chestLocation.getBlock().setType(Material.AIR);
        chestLocation.getWorld().playSound(chestLocation, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        chestLocation.getWorld().spawnParticle(Particle.REDSTONE, chestLocation,
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.WHITE, 20));
        RunicCore.getBossTagger().getActiveBossLootChests().remove(uuid);
    }
}
