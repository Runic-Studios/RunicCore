package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.SubClassEnum;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

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
     * @param position which sub-class? (1, 2, or 3)
     * @return an ItemStack icon
     */
    private ItemStack subClassItem(int position) {
        SubClassEnum subClassEnum = determineSubClass(position);
        String displayName = subClassEnum.getName();
        Material itemType = subClassEnum.getMaterial();

        ItemStack subClassItem = new ItemStack(itemType);
        ItemMeta meta = subClassItem.getItemMeta();
        if (meta == null) return subClassItem;
        meta.setDisplayName(ChatColor.GREEN + displayName);
        String lore = ChatColor.GRAY + "Open the skill tree for the " +
                ChatColor.GREEN + displayName +
                ChatColor.GRAY + " class! " + subClassEnum.getDescription();
        String[] loreArr = ChatPaginator.wordWrap(lore, 25);
        meta.setLore(Arrays.asList(loreArr));
        subClassItem.setItemMeta(meta);
        return subClassItem;
    }

    /**
     * Determines the appropriate sub-class based on player class and specified position
     * @param position (which sub-class? 1, 2, or 3)
     */
    private SubClassEnum determineSubClass(int position) {
        SubClassEnum subClassEnum = null;
        switch (RunicCoreAPI.getPlayerCache(player).getClassName().toLowerCase()) {
            case "archer":
                if (position == 1)
                    subClassEnum = SubClassEnum.MARKSMAN;
                else if (position == 2)
                    subClassEnum = SubClassEnum.SCOUT;
                else
                    subClassEnum = SubClassEnum.WARDEN;
                break;
            case "cleric":
                if (position == 1)
                    subClassEnum = SubClassEnum.BARD;
                else if (position == 2)
                    subClassEnum = SubClassEnum.EXEMPLAR;
                else
                    subClassEnum = SubClassEnum.PRIEST;
                break;
            case "mage":
                if (position == 1)
                    subClassEnum = SubClassEnum.CRYOMANCER;
                else if (position == 2)
                    subClassEnum = SubClassEnum.PYROMANCER;
                else
                    subClassEnum = SubClassEnum.WARLOCK;
                break;
            case "rogue":
                if (position == 1)
                    subClassEnum = SubClassEnum.ASSASSIN;
                else if (position == 2)
                    subClassEnum = SubClassEnum.DUELIST;
                else
                    subClassEnum = SubClassEnum.SWINDLER;
                break;
            case "warrior":
                if (position == 1)
                    subClassEnum = SubClassEnum.BERSERKER;
                else if (position == 2)
                    subClassEnum = SubClassEnum.GUARDIAN;
                else
                    subClassEnum = SubClassEnum.INQUISITOR;
                break;
        }
        return subClassEnum;
    }
}