package us.fortherealm.plugin.skills.caster.itemstack;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.skills.caster.itemstack.exception.ItemNotCasterItemException;

public class PlayerInteractWithCasterItemStack implements Listener {
	
	@EventHandler
	public void onInteractEvent(PlayerInteractEvent event) {
		
//		if (event.getItem() == null || !(event.getItem() instanceof CasterItemStack)) // Does not work because
//			return;                                                                   // spigot is lame

		System.out.println("preGoodStuff");

		if(event.getItem() == null)
			return;

		System.out.println("sortaGoodStuff");

		if(!(CasterItemStack.containsCasterSignature(event.getItem())))
			return;

		System.out.println("isCasterItem");

		event.setCancelled(true);

		CasterItemStack casterItem = CasterItemStack.getCasterItem(event.getItem());

		if(casterItem == null) {
			System.out.println("null caster");
			return;
		}

		System.out.println(casterItem.getName());

		switch (event.getAction()) {
			case RIGHT_CLICK_BLOCK:
			case RIGHT_CLICK_AIR:
				casterItem.executePrimarySkills(event.getPlayer());
				break;
			case LEFT_CLICK_BLOCK:
			case LEFT_CLICK_AIR:
				casterItem.executeSecondarySkills(event.getPlayer());
				break;
		}
	}
}
