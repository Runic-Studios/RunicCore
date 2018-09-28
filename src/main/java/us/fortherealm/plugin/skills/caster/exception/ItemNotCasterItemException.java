package us.fortherealm.plugin.skills.caster.exception;

import net.md_5.bungee.api.ChatColor;

public class ItemNotCasterItemException extends Exception {
	
	@Override
	public String getLocalizedMessage() {
		return ChatColor.RED + "The given ItemStack was not a caster item stack";
	}
	
}
