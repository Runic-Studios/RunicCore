package us.fortherealm.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CurrencySC extends SuperCommand {

    public CurrencySC() {
        super("ftrcore.currency");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(ChatColor.RED + "Usage: /currency give {player} {amount}");
    }
}
