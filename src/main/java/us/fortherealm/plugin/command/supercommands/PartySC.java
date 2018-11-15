package us.fortherealm.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.fortherealm.plugin.gui.GUIBuilder;
import us.fortherealm.plugin.util.CommandsUtil;

public class PartySC extends SuperCommand {
	
	public PartySC() {
		super("party.party");
	}
	
	@Override
	public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage
				(ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.GRAY + "Type "
                        + ChatColor.YELLOW + "/party help"
                        + ChatColor.GRAY + " to see commands!");
	}
}
