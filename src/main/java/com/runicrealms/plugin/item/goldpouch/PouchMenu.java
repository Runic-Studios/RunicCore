package com.runicrealms.plugin.item.goldpouch;

import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import org.bukkit.inventory.ItemStack;

public class PouchMenu {

    private final ItemGUI menu;
    private final ItemStack pouch;

    public PouchMenu(ItemGUI menu, ItemStack pouch) {
        this.menu = menu;
        this.pouch = pouch;
    }

    public ItemGUI getMenu() {
        return menu;
    }

    public ItemStack getPouch() {
        return pouch;
    }
}
