package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;

@CommandAlias("runicboss")
public class RunicBossCMD extends BaseCommand {

    private static final int CHEST_DURATION = 5; // seconds (60)

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

        // grab the list of players from the boss id tag
        // grab the list of drops from the corresponding file
        DungeonLocation dungeonLocation = DungeonLocation.getFromIdentifier(args[1]);
        if (dungeonLocation == null) {
            Bukkit.getServer().getLogger().info(ChatColor.DARK_RED + "Error loading dungeon boss drop!");
            return;
        }
        spawnChest(dungeonLocation);
        // todo: fill with drops (same inventory click logic as loot chests) (weighted bag)
        // todo: only players who contributed to boss can open
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(),
                () -> despawnChest(dungeonLocation.getChestLocation()), CHEST_DURATION * 20L);
    }

    private void spawnChest(DungeonLocation dungeonLocation) {
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
        RunicCore.getBossTagger().getActiveBossLootChests().add(chestLocation.getBlock());
    }

    private void despawnChest(Location chestLocation) {
        assert chestLocation.getWorld() != null;
        chestLocation.getBlock().setType(Material.AIR);
        chestLocation.getWorld().playSound(chestLocation, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        chestLocation.getWorld().spawnParticle(Particle.REDSTONE, chestLocation,
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.WHITE, 20));
        RunicCore.getBossTagger().getActiveBossLootChests().remove(chestLocation.getBlock());
    }
}
