package us.fortherealm.plugin.professions.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import us.fortherealm.plugin.command.supercommands.SuperCommand;

public class ToolSC extends SuperCommand {

    public ToolSC() {
        super("professions.tool");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

        sender.sendMessage(ChatColor.YELLOW + "Command usage: /gathertool give [tool] [tier]");
    }
}
