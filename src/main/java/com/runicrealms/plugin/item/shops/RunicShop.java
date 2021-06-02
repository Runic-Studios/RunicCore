package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicProfessions;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/*
 * All shops which require custom or unusual mechanics (item scrapper, jewel master etc.) extend this class.
 * This is different from RunicItemShops, which sell items and are more straightforward in functionality.
 */
// todo: have a global list of shop titles to check with a single listener, rather than a ton of them (register shop)
public abstract class RunicShop implements Listener {

    private String title;
    private ItemGUI itemGUI;

    public RunicShop() {
        title = "";
        itemGUI = new ItemGUI();
    }

    public RunicShop(String title) {
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

    protected void setupShop(String title, int size) {
        this.title = title;
        itemGUI = new ItemGUI(this.title, size, event -> {
        },
                RunicProfessions.getInstance()).setOption(size-1, new ItemStack(Material.BARRIER),
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
