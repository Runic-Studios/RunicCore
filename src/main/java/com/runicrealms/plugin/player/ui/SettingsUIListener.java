package com.runicrealms.plugin.player.ui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.model.SettingsData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SettingsUIListener implements Listener {

    private final Set<UUID> settingSlotOne = new HashSet<>();
    private final Set<UUID> settingSlotFour = new HashSet<>();

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
        } else if (event.getSlot() == 30) {
            player.closeInventory();
            player.sendMessage(ColorUtil.format("&aPlease enter the keyboard letter bound to your &2spell activation slot &lone &r&acontrol in chat, or type &ccancel&a:"));
            settingSlotOne.add(player.getUniqueId());
        } else if (event.getSlot() == 32) {
            player.closeInventory();
            player.sendMessage(ColorUtil.format("&aPlease enter the keyboard letter bound to your &2spell activation slot &lfour &r&acontrol in chat, or type &ccancel&a:"));
            settingSlotFour.add(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (settingSlotOne.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            settingSlotOne.remove(event.getPlayer().getUniqueId());
            SettingsData data = RunicCore.getSettingsManager().getSettingsData(event.getPlayer().getUniqueId());

            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                if (event.getMessage().equalsIgnoreCase("cancel")) {
                    event.getPlayer().openInventory(new SettingsUI(event.getPlayer(), data).getInventory());
                    return;
                }
                if (event.getMessage().length() != 1 || !event.getMessage().matches("^[A-Za-z0-9]$")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "This keybind must be one english character or number!");
                    return;
                }
                data.setSpellSlotOneDisplay(event.getMessage());
                event.getPlayer().openInventory(new SettingsUI(event.getPlayer(), data).getInventory());
            });
        } else if (settingSlotFour.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            settingSlotFour.remove(event.getPlayer().getUniqueId());
            SettingsData data = RunicCore.getSettingsManager().getSettingsData(event.getPlayer().getUniqueId());
            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                if (event.getMessage().equalsIgnoreCase("cancel")) {
                    event.getPlayer().openInventory(new SettingsUI(event.getPlayer(), data).getInventory());
                    return;
                }
                if (event.getMessage().length() != 1 || !event.getMessage().matches("^[A-Za-z0-9]$")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "This keybind must be one english character or number!");
                    return;
                }
                data.setSpellSlotFourDisplay(event.getMessage());
                event.getPlayer().openInventory(new SettingsUI(event.getPlayer(), data).getInventory());
            });
        }
    }
}
