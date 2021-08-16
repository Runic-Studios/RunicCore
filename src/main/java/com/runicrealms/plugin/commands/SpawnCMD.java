package com.runicrealms.plugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        ((Player) sender).getInventory().setHeldItemSlot(8); // set to hearthstone
        sender.sendMessage(ChatColor.AQUA + "Use your hearthstone to return to your hometown!");
        return true;
    }
}
