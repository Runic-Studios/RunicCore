package com.runicrealms.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TravelSC extends SuperCommand {

    public TravelSC() {
        super("runic.travel");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(ChatColor.RED + "Usage: /travel {player} {type} {x} {y} {z} {yaw} {pitch}");
    }
}
