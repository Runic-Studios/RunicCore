package com.runicrealms.plugin.item.commands;

import com.runicrealms.plugin.classes.ClassGUI;
import com.runicrealms.plugin.player.PlayerLevelUtil;
import com.runicrealms.plugin.player.commands.SetSC;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HearthstoneCMD implements SubCommand {

    private SetSC set;
    private Plugin plugin = RunicCore.getInstance();
    private static List<UUID> hearthstoneChangers = new ArrayList<>();
    public static List<UUID> getHearthstoneChangers() { return hearthstoneChangers; }

    public HearthstoneCMD(SetSC set) {
        this.set = set;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {
        // if the sender does not specify a player
        if (args.length != 3 && args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set hearthstone [player] [location] (cost)");
        } else {

            Player pl = Bukkit.getPlayer(args[1]);
            if (pl == null) return;
            String location = args[2];

            if (args.length == 3) {
                ClassGUI.setupHearthstone(pl, location);
            } else {
                int cost = Integer.parseInt(args[3]);
                pl.sendMessage(ColorUtil.format("&7[1/1] &eInnkeeper: &fYou'd like to change you &bhearthstone location to &a" + location + "&f?" +
                        "\nThat will be &6" + cost + " coins." +
                        "\n&7Type &a/confirm &7or &c/deny"));
                hearthstoneChangers.add(pl.getUniqueId());
                //ClassGUI.setupHearthstone(pl, location);
            }
        }
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        // if the sender does not specify a player
        if (args.length != 3 && args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set hearthstone [player] [location]");
        } else {
            this.onConsoleCommand(sender, args);
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        // if the sender does not specify a player
        if (args.length != 3 && args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set hearthstone [player] [location]");
        } else {
            this.onConsoleCommand(sender, args);
        }
    }

    @Override
    public String permissionLabel() {
        return "set.hearthstone";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
