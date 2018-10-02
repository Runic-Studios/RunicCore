package us.fortherealm.plugin.skills.caster.itemstack.exception;

import net.md_5.bungee.api.ChatColor;

public class ItemMetaLockedException extends Exception {
	
	@Override
	public String getLocalizedMessage() {
		return ChatColor.RED + "Caster item meta can NOT be changed! (Sorry!)";
	}
	
}
