package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;


@CommandAlias("gamemode|gm")
public class GameModeCMD extends BaseCommand {

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        if (player.hasPermission("runiccore.gamemode.adventure")) {
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage(ChatColor.GREEN + "Set your gamemode to adventure");
        } else {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
        }
    }

    @Subcommand("creative|1")
    @CommandPermission("runiccore.gamemode.creative")
    public void onCommandCreative(Player player) {
        player.setGameMode(GameMode.CREATIVE);
        player.sendMessage(ChatColor.GREEN + "Set your gamemode to creative");
    }

    @Subcommand("adventure|2")
    @CommandPermission("runiccore.gamemode.adventure")
    public void onCommandAdventure(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.sendMessage(ChatColor.GREEN + "Set your gamemode to adventure");
    }

    @Subcommand("survival|0")
    @CommandPermission("runiccore.gamemode.survival")
    public void onCommandSurvival(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage(ChatColor.GREEN + "Set your gamemode to survival");
    }

    @Subcommand("spectator|3")
    @CommandPermission("runiccore.gamemode.spectator")
    public void onCommandSpectator(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(ChatColor.GREEN + "Set your gamemode to spectator");
    }

}