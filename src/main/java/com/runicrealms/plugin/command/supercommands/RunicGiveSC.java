package com.runicrealms.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RunicGiveSC extends SuperCommand {

    public RunicGiveSC() {
        super("runic.give");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

        sender.sendMessage(ChatColor.YELLOW + "Command usage: /give [thing]");
    }
}
