package com.runicrealms.plugin.player.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;

import java.util.List;

public class SetProfLevelCMD implements SubCommand {

    private SetSC set;
    private Plugin plugin = RunicCore.getInstance();

    public SetProfLevelCMD(SetSC set) {
        this.set = set;
    }


    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onUserCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        // if the sender does not specify a player
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set proflevel [level] or /set proflevel [player] [level]");
        } else if (args.length == 2) {
            RunicCore.getInstance().getConfig().set(sender.getUniqueId() + ".info.prof.level", Integer.parseInt(args[1]));
            RunicCore.getInstance().getConfig().set(sender.getUniqueId() + ".info.prof.exp", 0);
            RunicCore.getInstance().saveConfig();
            RunicCore.getInstance().reloadConfig();
            RunicCore.getScoreboardHandler().updatePlayerInfo(sender);
            RunicCore.getScoreboardHandler().updateSideInfo(sender);
        } else if (args.length == 3) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) return;
            RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.prof.level", Integer.parseInt(args[1]));
            RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.prof.exp", 0);
            RunicCore.getInstance().saveConfig();
            RunicCore.getInstance().reloadConfig();
            RunicCore.getScoreboardHandler().updatePlayerInfo(player);
            RunicCore.getScoreboardHandler().updateSideInfo(player);
        }
    }

    @Override
    public String permissionLabel() {
        return "set.proflevel";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
