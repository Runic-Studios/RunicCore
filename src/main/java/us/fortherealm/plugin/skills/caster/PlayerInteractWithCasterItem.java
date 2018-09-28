package us.fortherealm.plugin.skills.caster;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.caster.exception.ItemNotCasterItemException;

public class PlayerInteractWithCasterItem implements Listener {
	
	@EventHandler
	public void onInteractEvent(PlayerInteractEvent event) {
		
		if(!(Main.getItemCasterManager().isItemCasterItem(event.getItem())))
			return;
		
		ItemStack item = event.getItem();
		ItemCasterManager casterManager = Main.getItemCasterManager();
		
		try {
			CasterStorage<ItemStack> casterStorage = casterManager.getCasterStorage(item);
			
			if(!(casterStorage.getChildCasters().keySet().contains(item)))
				casterStorage.addLinkedCaster(item);
			
			Caster caster = casterStorage.getChildCasters().get(item);
			
			event.setCancelled(true);
			
			switch (event.getAction()) {
				case RIGHT_CLICK_BLOCK:
				case RIGHT_CLICK_AIR:
					caster.executePrimarySkills(event.getPlayer());
					break;
				case LEFT_CLICK_BLOCK:
				case LEFT_CLICK_AIR:
					caster.executeSecondarySkills(event.getPlayer());
					break;
			}
			
		} catch (ItemNotCasterItemException e) {
			e.printStackTrace();
			e.getLocalizedMessage();
			return;
		}
	}
}
