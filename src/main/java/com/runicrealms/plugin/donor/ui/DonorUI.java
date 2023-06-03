package com.runicrealms.plugin.donor.ui;

import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DonorUI implements InventoryHolder {

    private static final ItemStack topElement;
    private static final ItemStack weaponSkinElement;
    private static final ItemStack boostsElement;
    private static final ItemStack additionalPerksElement;

    static {
        topElement = new ItemStack(Material.EMERALD);
        ItemMeta meta = topElement.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&cDonor Perk Menu"));
        meta.setLore(ChatUtils.formattedText("&7View perks exclusive to your rank as a donor!"));
        topElement.setItemMeta(meta);
        weaponSkinElement = new ItemStack(Material.WOODEN_AXE);
        Damageable damageable = (Damageable) weaponSkinElement.getItemMeta();
        damageable.setDamage(6);
        weaponSkinElement.setItemMeta((ItemMeta) damageable);
        meta = weaponSkinElement.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        meta.setUnbreakable(true);
        meta.setDisplayName(ColorUtil.format("&eOpen Weapon Appearances"));
        meta.setLore(ChatUtils.formattedText("&7Modify the appearance of your weapons and artifacts with custom weapon skins exclusively for donors"));
        weaponSkinElement.setItemMeta(meta);
        boostsElement = new ItemStack(Material.EXPERIENCE_BOTTLE);
        meta = boostsElement.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.setDisplayName(ColorUtil.format("&aActivate Experience Boosts"));
        meta.setLore(ChatUtils.formattedText("&7View your combat, crafting, and gathering boosts, you can buy more at &estore.runicrealms.com"));
        boostsElement.setItemMeta(meta);
        additionalPerksElement = new ItemStack(Material.WRITABLE_BOOK);
        meta = additionalPerksElement.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&cAdditional Donor perks"));
        meta.setLore(List.of(ColorUtil.format("&7TODO")));
        additionalPerksElement.setItemMeta(meta);
    }

    private final Player player;
    private final Inventory inventory;

    public DonorUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&cDonor Perk Menu"));
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
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(4, topElement);
        this.inventory.setItem(20, weaponSkinElement);
        this.inventory.setItem(22, boostsElement);
        this.inventory.setItem(24, additionalPerksElement);
    }

}
