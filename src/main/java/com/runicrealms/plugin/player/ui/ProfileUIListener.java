package com.runicrealms.plugin.player.ui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SettingsData;
import com.runicrealms.plugin.player.settings.SettingsUI;
import com.runicrealms.plugin.rdb.RunicDatabase;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import redis.clients.jedis.Jedis;

public class ProfileUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof ProfileUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        ProfileUI profileUI = (ProfileUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(profileUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (profileUI.getInventory().getItem(event.getRawSlot()) == null) return;

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        // achievements slot (20) is handled in achievements
        if (event.getSlot() == 0) {
            player.closeInventory();
        } else if (event.getSlot() == 24) {
            try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                player.openInventory(new SettingsUI
                        (
                                player,
                                (SettingsData) RunicCore.getSettingsManager().loadSessionData(player.getUniqueId(), jedis)
                        ).getInventory());
            }
        }
    }

}
