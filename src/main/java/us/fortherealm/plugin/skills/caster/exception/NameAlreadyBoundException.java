package us.fortherealm.plugin.skills.caster.exception;

import net.md_5.bungee.api.ChatColor;

public class NameAlreadyBoundException extends Exception {
	
	@Override
	public String getLocalizedMessage() {
		return ChatColor.RED + "The name has already been bound to another item";
	}
	
}
