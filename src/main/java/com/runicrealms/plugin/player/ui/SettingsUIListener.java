package com.runicrealms.plugin.player.ui;

import com.runicrealms.plugin.common.util.GUIUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class SettingsUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof SettingsUI)) return;
        // prevent clicking items in player inventory
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        SettingsUI settingsUI = (SettingsUI) event.getClickedInventory().getHolder();
        if (settingsUI == null) return;
        // insurance
        if (!event.getWhoClicked().equals(settingsUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (settingsUI.getInventory().getItem(event.getRawSlot()) == null) return;
        ItemStack item = event.getCurrentItem();
        Material material = item.getType();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);
        // Toggle spell cast UI
        if (material == GUIUtil.BACK_BUTTON.getType()) {
            player.openInventory(new ProfileUI(player).getInventory());
        } else if (material == Material.PAPER) {
            boolean castMenuEnabled = settingsUI.getSettingsData().isCastMenuEnabled();
            settingsUI.getSettingsData().setCastMenuEnabled(!castMenuEnabled);
            player.openInventory(new SettingsUI(player, settingsUI.getSettingsData()).getInventory());
        } else if (material == Material.POPPED_CHORUS_FRUIT) {
            boolean openRunestoneInCombat = settingsUI.getSettingsData().shouldOpenRunestoneInCombat();
            settingsUI.getSettingsData().setOpenRunestoneInCombat(!openRunestoneInCombat);
            player.openInventory(new SettingsUI(player, settingsUI.getSettingsData()).getInventory());
        }
    }
}
