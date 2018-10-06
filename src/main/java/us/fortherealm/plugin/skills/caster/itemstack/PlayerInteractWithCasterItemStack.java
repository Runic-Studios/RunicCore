package us.fortherealm.plugin.skills.caster.itemstack;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractWithCasterItemStack implements Listener {
	
	@EventHandler
	public void onInteractEvent(PlayerInteractEvent event) {
		
//		if (event.getItem() == null || !(event.getItem() instanceof CasterItemStack)) // Does not work because
//			return;                                                                   // spigot is lame

		if(event.getItem() == null)
			return;

		if(!(CasterItemStack.containsCasterSignature(event.getItem())))
			return;

		CasterItemStack casterItem = CasterItemStack.getCasterItem(event.getItem());

		if(casterItem == null)
			return;

		switch (event.getAction()) {
			case RIGHT_CLICK_BLOCK:
			case RIGHT_CLICK_AIR:
				casterItem.executeSecondarySkills(event.getPlayer());
				break;
			case LEFT_CLICK_BLOCK:
			case LEFT_CLICK_AIR:
				casterItem.executePrimarySkills(event.getPlayer());
				break;
		}
	}
}
