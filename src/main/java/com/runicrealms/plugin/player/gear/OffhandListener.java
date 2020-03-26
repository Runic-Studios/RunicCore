package com.runicrealms.plugin.player.gear;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.codingforcookies.armorequip.ArmorType;
import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class OffhandListener implements Listener {

    /**
     * Adds offhands changed from inventory to the armorequip event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player pl = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;
        if (pl.getGameMode() != GameMode.SURVIVAL) return;

        if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) {
            if (e.getCurrentItem() != null && AttributeUtil.getCustomString(e.getCurrentItem(), "offhand").equals("true")) {
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.CHESTPLATE, e.getCurrentItem(), e.getCursor());
                Bukkit.getPluginManager().callEvent(armorEquipEvent);
            }
        } else {
            int itemslot = e.getSlot();
            if (itemslot != 40) return; // offhand slot

            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(pl, ArmorEquipEvent.EquipMethod.DRAG, ArmorType.CHESTPLATE, e.getCurrentItem(), e.getCursor());
            Bukkit.getPluginManager().callEvent(armorEquipEvent);
        }
    }
}
