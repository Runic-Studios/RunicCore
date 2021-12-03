package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;

@CommandAlias("runicboss")
public class RunicBossCMD extends BaseCommand {

    private static final int CHEST_DURATION = 60; // seconds

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
        Location chestLocation = dungeonLocation.getChestLocation();
        chestLocation.getBlock().setType(Material.CHEST);
        // todo: fill with drops (same inventory click logic as loot chests)
        // todo: only players who contributed to boss can open
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(),
                () -> chestLocation.getBlock().setType(Material.AIR), CHEST_DURATION * 20L);
    }
}
