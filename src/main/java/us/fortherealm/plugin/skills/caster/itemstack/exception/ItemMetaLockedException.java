package us.fortherealm.plugin.skills.caster.itemstack.exception;


import org.bukkit.ChatColor;

public class ItemMetaLockedException extends Exception {
	
	@Override
	public String getLocalizedMessage() {
		return ChatColor.RED + "Caster item meta can NOT be changed! (Sorry!)";
	}
	
}
