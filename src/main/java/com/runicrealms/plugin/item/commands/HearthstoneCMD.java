package com.runicrealms.plugin.item.commands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import com.runicrealms.plugin.item.hearthstone.HearthstoneLocation;
import com.runicrealms.plugin.player.commands.SetSC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class HearthstoneCMD implements SubCommand {

    private final SetSC set;
    private final Plugin plugin = RunicCore.getInstance();

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
                if (HearthstoneLocation.getLocationFromIdentifier(location) != null) {
                    pl.getInventory().setItem(8, HearthstoneLocation.getFromIdentifier(location).getItemStack());
                }
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
