package com.runicrealms.plugin.donor.boost.ui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.donor.boost.api.StoreBoost;
import com.runicrealms.plugin.donor.ui.DonorUI;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class BoostsUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof BoostsUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        BoostsUI boostsUI = (BoostsUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(boostsUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (boostsUI.getInventory().getItem(event.getRawSlot()) == null) return;

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (event.getSlot() == 0) {
            player.openInventory(new DonorUI(player).getInventory());
        } else if (event.getSlot() == 20 && RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.COMBAT)) {
            if (RunicCore.getBoostAPI().isBoostActive(StoreBoost.COMBAT)) {
                player.sendMessage(ChatColor.RED + "You cannot activate a combat boost while another one is already activated!");
                player.closeInventory();
            } else player.openInventory(new BoostConfirmUI(player, StoreBoost.COMBAT).getInventory());
        } else if (event.getSlot() == 22 && RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.CRAFTING)) {
            if (RunicCore.getBoostAPI().isBoostActive(StoreBoost.CRAFTING)) {
                player.sendMessage(ChatColor.RED + "You cannot activate a crafting boost while another one is already activated!");
                player.closeInventory();
            } else player.openInventory(new BoostConfirmUI(player, StoreBoost.CRAFTING).getInventory());
        } else if (event.getSlot() == 24 && RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), StoreBoost.GATHERING)) {
            if (RunicCore.getBoostAPI().isBoostActive(StoreBoost.GATHERING)) {
                player.sendMessage(ChatColor.RED + "You cannot activate a gathering boost while another one is already activated!");
                player.closeInventory();
            } else player.openInventory(new BoostConfirmUI(player, StoreBoost.GATHERING).getInventory());
        }
    }

}
