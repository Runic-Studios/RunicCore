package us.fortherealm.plugin.skills.exceptions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class SkillNotAvailableException extends Exception {
	
	public void printErrorToConsole() {
		Bukkit.getServer().getConsoleSender().sendMessage(
				ChatColor.RED + "Attempted skill is not a skill available to the caster"
		);
	}
	
	
}
