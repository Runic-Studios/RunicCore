package com.runicrealms.plugin.player.gear;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;

/**
 * Prevents off-hands from being clicked or dragged into the inventory
 */
public class OffhandListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST) // executes FIRST
    public void onInventoryClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        if (e.getSlot() != 40) return; // off-hand slot
        e.setCancelled(true);
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
        player.sendMessage(ChatColor.GRAY + "To equip an offhand, hold it and right-click!");

        // todo: cleanup this logic
//        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
//            if (e.getCurrentItem() != null && AttributeUtil.getCustomString(e.getCurrentItem(), "offhand").equals("true")) {
//                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.CHESTPLATE, e.getCurrentItem(), e.getCursor());
//                Bukkit.getPluginManager().callEvent(armorEquipEvent);
//            }
//        } else {
//            int itemslot = e.getSlot();
//            if (itemslot != 40) return; // offhand slot
//            if (e.getCursor() != null
//                    && e.getCursor().getType() != Material.AIR
//                    && !AttributeUtil.getCustomString(e.getCursor(), "offhand").equals("true")) {
//
//                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
//                player.sendMessage(ChatColor.RED + "You can only equip off-hand items in the off-hand slot!");
//                e.setCancelled(true);
//            }
//            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.CHESTPLATE, e.getCurrentItem(), e.getCursor());
//            Bukkit.getPluginManager().callEvent(armorEquipEvent);
//        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!e.getInventory().getType().equals(InventoryType.CRAFTING)) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        if (e.getInventorySlots().contains(40)) {
            e.setCancelled(true);
        }
    }
}
