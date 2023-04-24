package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;

@CommandAlias("gamemode|gm")
public class GameModeCMD extends BaseCommand {

    public GameModeCMD() {
        RunicCore.getCommandManager().getCommandCompletions().registerAsyncCompletion("gamemodes", context -> new HashSet<String>() {{
            add("survival");
            add("creative");
            add("adventure");
        }});
    }

    // gamemode [gamemode]

    @Default
    @CatchUnknown
    @CommandCompletion("@gamemodes")
    @Conditions("is-console-or-op")
    public void onCommand(CommandSender commandSender, String[] args) {
        if (args.length != 1) {
            commandSender.sendMessage(ChatColor.YELLOW + "Error, incorrect arguments. Usage: gamemode [gamemode]");
            return;
        }
//        Player player = Bukkit.getPlayer(args[0]);
//        if (player == null) return;
        // todo: disable from console
        // todo: aliases
        Player player = (Player) commandSender;
        String gamemode = args[0];
        if (gamemode.equalsIgnoreCase("adventure")) {
            player.setGameMode(GameMode.ADVENTURE);
        } else if (gamemode.equalsIgnoreCase("creative")) {
            player.setGameMode(GameMode.CREATIVE);
        } else if (gamemode.equalsIgnoreCase("survival")) {
            player.setGameMode(GameMode.SURVIVAL);
        } else if (gamemode.equalsIgnoreCase("spectator")) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.sendMessage(ChatColor.RED + "Error, please enter a valid gamemode");
        }
    }
}