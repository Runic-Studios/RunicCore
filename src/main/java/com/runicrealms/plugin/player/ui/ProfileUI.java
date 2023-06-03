package com.runicrealms.plugin.player.ui;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProfileUI implements InventoryHolder {

    private static final ItemStack achievementsIcon;
    private static final ItemStack settingsIcon;

    static {
        achievementsIcon = new ItemStack(Material.MAP);
        ItemMeta meta = achievementsIcon.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&eAchievements"));
        meta.setLore(List.of(
                "",
                ColorUtil.format("&6&lCLICK"),
                ColorUtil.format("&7To view achievements!")));
        achievementsIcon.setItemMeta(meta);

        settingsIcon = new ItemStack(Material.BELL);
        meta = settingsIcon.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&eSettings"));
        meta.setLore(List.of(
                "",
                ColorUtil.format("&6&lCLICK"),
                ColorUtil.format("&7To modify your settings")));
        settingsIcon.setItemMeta(meta);
    }

    private final Player player;
    private final Inventory inventory;

    public ProfileUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&e" + player.getName() + "'s Profile"));
        generateMenu();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    private void generateMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(inventory);
        this.inventory.setItem(0, GUIUtil.CLOSE_BUTTON);
        ItemStack topElement = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) topElement.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        skullMeta.setDisplayName(ColorUtil.format("&e" + player.getName() + "'s Profile"));
        topElement.setItemMeta(skullMeta);
        this.inventory.setItem(4, topElement);
        this.inventory.setItem(20, achievementsIcon);
        this.inventory.setItem(24, settingsIcon);
    }


}
