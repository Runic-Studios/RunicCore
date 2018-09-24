package us.fortherealm.plugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class EquipArtifactEvent implements Listener {
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		
		Player player = (Player) e.getWhoClicked();
		int itemSlot = e.getSlot();
		ItemStack cursorItem = player.getItemOnCursor();
		ItemStack oldItem = player.getInventory().getItem(1);
		if(oldItem == null)
			return;
		ItemMeta cursorItemMeta = cursorItem.getItemMeta();
		ItemMeta oldItemMeta = oldItem.getItemMeta();
		
		if (e.getClickedInventory() == null || player.getItemOnCursor() == null || !cursorItem.hasItemMeta()) return;
		
		if (itemSlot == 0 && player.getGameMode() == GameMode.SURVIVAL
				&& e.getClickedInventory().getType().equals(InventoryType.PLAYER)
				&& cursorItem.getType() != Material.AIR && cursorItem.hasItemMeta()) {
			
			List<String> lore = cursorItemMeta.getLore();
			String loreAsString = ChatColor.stripColor
					(String.join(" ", player.getItemOnCursor().getItemMeta().getLore()));
			
			if (loreAsString.contains("Artifact")) {
				
				lore.add(ChatColor.DARK_GRAY + "Equipped");
				cursorItemMeta.setLore(lore);
				cursorItem.setItemMeta(cursorItemMeta);
				
				String slotLore = ChatColor.stripColor
						(String.join(" ", oldItem.getItemMeta().getLore()));
				
				if (slotLore.contains("Equipped")) {
					
					List<String> oldItemLore = oldItem.getItemMeta().getLore();
					oldItemLore.remove(ChatColor.DARK_GRAY + "Equipped");
					oldItemMeta.setLore(oldItemLore);
					oldItem.setItemMeta(oldItemMeta);
				}
			}
		}
	}
}


