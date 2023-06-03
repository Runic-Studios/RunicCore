package com.runicrealms.plugin.donor.boost.ui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class BoostConfirmUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof BoostConfirmUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        BoostConfirmUI boostConfirmUI = (BoostConfirmUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(boostConfirmUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (boostConfirmUI.getInventory().getItem(event.getRawSlot()) == null) return;

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (event.getSlot() == 11 && RunicCore.getBoostAPI().hasStoreBoost(player.getUniqueId(), boostConfirmUI.getBoost())) {
            player.closeInventory();
            if (RunicCore.getBoostAPI().hasDelayedRestart()) {
                player.sendMessage(ColorUtil.format("&cA player has already activated a boost that delayed an automatic server restart. " +
                        "Please wait until the server restarts automatically to activate your boost. " +
                        "Thank you for helping ensure our server remains performant."));
            } else RunicCore.getBoostAPI().activateStoreBoost(player, boostConfirmUI.getBoost());
        } else if (event.getSlot() == 15) {
            player.openInventory(new BoostsUI(player).getInventory());
        }
    }

}
