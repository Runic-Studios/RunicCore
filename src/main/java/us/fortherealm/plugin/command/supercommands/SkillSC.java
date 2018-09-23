package us.fortherealm.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import us.fortherealm.plugin.util.CommandsUtil;

public class SkillSC extends SuperCommand {
	
	
	public SkillSC() {
		super("skill.skill");
	}
	
	@Override
	public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "Basic skills command");
	}
}
