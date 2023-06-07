package com.runicrealms.plugin.player.ui;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.model.SettingsData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SettingsUI implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;
    private final SettingsData settingsData;

    public SettingsUI(Player player, SettingsData settingsData) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&ePlayer Settings Menu"));
        this.player = player;
        this.settingsData = settingsData;
        generateMenu();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public SettingsData getSettingsData() {
        return settingsData;
    }

    /**
     * Generates the inventory associated w/ this GUI, ordering perks
     */
    private void generateMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        boolean castMenuEnabled = this.settingsData.isCastMenuEnabled();
        boolean openRunestoneInCombat = this.settingsData.shouldOpenRunestoneInCombat();
        this.inventory.setItem(20, GUIUtil.dispItem(
                Material.PAPER,
                ChatColor.LIGHT_PURPLE + "Display Spell Cast UI",
                new String[]{ChatColor.YELLOW + "Enabled: " + (castMenuEnabled ? ChatColor.GREEN : ChatColor.RED) + castMenuEnabled}
        ));
        this.inventory.setItem(24, GUIUtil.dispItem(
                Material.POPPED_CHORUS_FRUIT,
                ChatColor.LIGHT_PURPLE + "Open Runestone in Combat",
                new String[]{ChatColor.YELLOW + "Enabled: " + (openRunestoneInCombat ? ChatColor.GREEN : ChatColor.RED) + openRunestoneInCombat}
        ));
    }
}