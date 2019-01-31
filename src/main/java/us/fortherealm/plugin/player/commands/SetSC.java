package us.fortherealm.plugin.player.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.command.supercommands.SuperCommand;

public class SetSC extends SuperCommand {

    public SetSC() {
        super("player.set");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "Usage: /set [argument] or /set [player] [argument]");
        }
    }
}
