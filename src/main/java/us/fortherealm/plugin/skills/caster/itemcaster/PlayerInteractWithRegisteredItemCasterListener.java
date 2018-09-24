package us.fortherealm.plugin.skills.caster.itemcaster;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerInteractWithRegisteredItemCasterListener implements Listener {
	
	private static List<ItemCaster> registeredItemCasters = new ArrayList<>();
	
	@EventHandler
	public void onInteractEvent(PlayerInteractEvent event) {
		for(ItemCaster registeredItemCaster : registeredItemCasters) {
			
			if(!event.getItem().equals(registeredItemCaster.getItem()))
				continue;
			
			event.setCancelled(true);
			
			switch(event.getAction()) {
			
			}
			
		}
	}
	
	
	public static void addRegisteredItemCaster(ItemCaster itemCaster) {
		registeredItemCasters.add(itemCaster);
	}
	
	public static void delRegisteredItemCaster(ItemCaster itemCaster) {
		registeredItemCasters.remove(itemCaster);
	}
	
}
