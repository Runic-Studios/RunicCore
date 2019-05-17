package com.runicrealms.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpellpointSC extends SuperCommand {

    public SpellpointSC() {
        super("player.spellpoint");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "Usage: /spellpoint give [player]");
        }
    }
}
