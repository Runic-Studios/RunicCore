package com.runicrealms.plugin.player.settings;

import com.runicrealms.plugin.model.SettingsData;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
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
        openMenu();
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
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        boolean castMenuEnabled = this.settingsData.isCastMenuEnabled();
        this.inventory.setItem(22, GUIUtil.dispItem(
                Material.PAPER,
                "Spell Cast UI",
                new String[]{castMenuEnabled + ""}
        ));
    }
}