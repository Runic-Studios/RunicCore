package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.ArmorType;
import com.runicrealms.plugin.ItemType;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

/**
 * Prevents off-hands from being clicked or dragged into the inventory
 */
public class OffhandListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST) // executes FIRST
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        ItemStack oldItem = event.getCurrentItem();
        ItemStack newItem = event.getCursor();
        if (newItem == null) return;
        if (!(event.getSlot() == 40 || event.getClick() == ClickType.SWAP_OFFHAND)) return;

        ItemType itemType = ItemType.matchType(newItem);
        if (itemType != ItemType.OFFHAND && newItem.getType() != Material.AIR) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Only offhands can be equipped in this slot!");
            event.setCancelled(true);
            return;
        }

        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent
                (
                        player,
                        ArmorEquipEvent.EquipMethod.PICK_DROP,
                        ArmorType.OFFHAND,
                        oldItem,
                        newItem
                );
        Bukkit.getPluginManager().callEvent(armorEquipEvent);
    }

    /**
     * Prevents a bug allowing non-off-hand items to be placed in off-hand slot
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!event.getInventory().getType().equals(InventoryType.CRAFTING)) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (!event.getInventorySlots().contains(40)) return;
        event.setCancelled(true);
    }
}
