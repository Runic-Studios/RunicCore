package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.classes.SubClassEnum;
import com.runicrealms.plugin.classes.utilities.SubClassUtil;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SubClassGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;

    public SubClassGUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 27, ColorUtil.format("&aChoose a sub-class!"));
        this.player = player;
        openMenu();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        this.inventory.setItem(0, GUIUtil.backButton());
        this.inventory.setItem(11, subClassItem(1));
        this.inventory.setItem(13, subClassItem(2));
        this.inventory.setItem(15, subClassItem(3));
    }

    /**
     * Adds an ItemStack with some type of glazed terracotta to represent a sub-class.
     *
     * @param position which sub-class? (1, 2, or 3)
     * @return an ItemStack icon
     */
    private ItemStack subClassItem(int position) {
        SubClassEnum subClassEnum = SubClassUtil.determineSubClass(player, position);
        String displayName = subClassEnum.getName();
        ItemStack subClassItem = subClassEnum.getItemStack();
        ItemMeta meta = subClassItem.getItemMeta();
        if (meta == null) return subClassItem;
        meta.setDisplayName(ChatColor.GREEN + displayName);
        String lore = ChatColor.GRAY + "Open the skill tree for the " +
                ChatColor.GREEN + displayName +
                ChatColor.GRAY + " class! " + subClassEnum.getDescription();
        meta.setLore(ChatUtils.formattedText(lore));
        subClassItem.setItemMeta(meta);
        return subClassItem;
    }
}