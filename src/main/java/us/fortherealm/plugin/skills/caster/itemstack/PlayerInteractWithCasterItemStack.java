package us.fortherealm.plugin.skills.caster.itemstack;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractWithCasterItemStack implements Listener {
	
	@EventHandler
	public void onInteractEvent(PlayerInteractEvent event) {

		performCast(event.getItem(), event.getAction(), event.getPlayer());

	}

	@EventHandler
	public void onDamageEvent(EntityDamageByEntityEvent event) {

		if(!(event.getDamager() instanceof Player))
			return;

		Player player = (Player) event.getDamager();

		performCast(player.getInventory().getItemInMainHand(), Action.LEFT_CLICK_AIR, player);

	}

	private void performCast(ItemStack item, Action action, Player player) {
		if(player == null)
			return;

		if(item == null)
			return;

		if(!(CasterItemStack.containsCasterSignature(item)))
			return;

		CasterItemStack casterItem = CasterItemStack.getCasterItem(item);

		if(casterItem == null)
			return;

		switch (action) {
			case RIGHT_CLICK_BLOCK:
			case RIGHT_CLICK_AIR:
				casterItem.executeSecondarySkills(player);
				break;
			case LEFT_CLICK_BLOCK:
			case LEFT_CLICK_AIR:
				casterItem.executePrimarySkills(player);
				break;
		}
	}
}
