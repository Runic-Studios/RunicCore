package com.runicrealms.plugin.player.settings;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.rdb.RunicDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

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
        if (material == GUIUtil.CLOSE_BUTTON.getType()) {
            player.closeInventory();
        } else if (material == Material.PAPER) {
            TaskChain<?> chain = RunicCore.newChain();
            chain
                    .asyncFirst(() -> {
                        boolean castMenuEnabled = settingsUI.getSettingsData().isCastMenuEnabled();
                        settingsUI.getSettingsData().setCastMenuEnabled(!castMenuEnabled);
                        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
                            try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                                settingsUI.getSettingsData().writeToJedis(player.getUniqueId(), jedis);
                            }
                        });
                        return settingsUI.getSettingsData().isCastMenuEnabled();
                    })
                    .syncLast(isCastMenuEnabled -> player.openInventory(new SettingsUI(player, settingsUI.getSettingsData()).getInventory()))
                    .execute();
        }
    }
}
