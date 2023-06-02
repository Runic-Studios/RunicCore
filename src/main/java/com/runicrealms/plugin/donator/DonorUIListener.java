package com.runicrealms.plugin.donator;

import com.runicrealms.runicitems.weaponskin.ui.WeaponryUI;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class DonorUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof DonorUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        DonorUI donorUI = (DonorUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(donorUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (donorUI.getInventory().getItem(event.getRawSlot()) == null) return;

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (event.getSlot() == 20) {
            player.openInventory(new WeaponryUI(player).getInventory());
        } else if (event.getSlot() == 23) {
            // TODO open boosts menu
        } else if (event.getSlot() == 26) {
            // TODO open additional perks menu
        }
    }

}
