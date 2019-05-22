package com.runicrealms.plugin.professions.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.runicrealms.plugin.command.supercommands.SuperCommand;

public class GathertoolSC extends SuperCommand {

    public GathertoolSC() {
        super("professions.tool");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

        sender.sendMessage(ChatColor.YELLOW + "Command usage: /gathertool give [tool] [tier]");
    }
}
