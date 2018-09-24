package us.fortherealm.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SkillSC extends SuperCommand {
	
	public SkillSC() {
		super("oldskills.oldskills");
	}
	
	@Override
	public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "Basic oldskills command");
	}
}
