package us.fortherealm.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.fortherealm.plugin.util.CommandsUtil;

public class PartySC extends SuperCommand {
	
	public PartySC() {
		super("party.party");
	}
	
	@Override
	public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO: Open PartyCMD GUI
		sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
				ChatColor.RED + "Type " + ChatColor.AQUA + "/party help" + ChatColor.RED + " to see commands!");
		return;
	}
}
