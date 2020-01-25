package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicProfessions;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * All shops which require custom mechanics (artifact forge, item scrapper, etc.) extend this class
 */
public abstract class Shop implements Listener {

    private String title;
    private ItemGUI itemGUI;

    public Shop() {
        title = "";
        itemGUI = new ItemGUI();
    }

    public Shop(String title) {
        this.title = title;
        itemGUI = new ItemGUI();
    }

    protected void setupShop(Player pl) {
        title = "&eShop";
        itemGUI = new ItemGUI(title, 9, event -> {
        },
                RunicProfessions.getInstance()).setOption(8, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0, false);
    }

    protected void setupShop(String title, boolean fillSlots) {
        this.title = title;
        itemGUI = new ItemGUI(this.title, 9, event -> {
        },
                RunicProfessions.getInstance()).setOption(8, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0, false);
        if (!fillSlots) return;
        for (int i = 0; i < 8; i++) {
            itemGUI.setOption(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE), "&7", "", 0, false);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String s) {
        this.title = s;
    }

    public ItemGUI getItemGUI() {
        return itemGUI;
    }

    public void setItemGUI(ItemGUI itemGUI) {
        this.itemGUI = itemGUI;
    }
}
