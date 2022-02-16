package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicProfessions;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/**
 * All shops which require custom or unusual mechanics (item scrapper, jewel master etc.) extend this class.
 * This is different from RunicItemShops, which sell items and are more straightforward in functionality.
 */
public abstract class RunicShop implements Listener {

    private final int shopSize;
    private final ItemStack icon;
    private final String name;
    private final Collection<Integer> runicNpcIds;
    private ItemGUI itemGUI;

    public RunicShop(int shopSize, ItemStack icon, String name, Collection<Integer> runicNpcIds) {
        this.shopSize = shopSize;
        this.icon = icon;
        this.name = name;
        this.runicNpcIds = runicNpcIds;
        itemGUI = new ItemGUI();
    }

    protected void setupShop(Player player) {
        itemGUI = new ItemGUI(this.name, this.shopSize, event -> {
        }, RunicProfessions.getInstance());
    }

    public int getShopSize() {
        return shopSize;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public Collection<Integer> getRunicNpcIds() {
        return runicNpcIds;
    }

    public ItemGUI getItemGUI() {
        return itemGUI;
    }

    public void setItemGUI(ItemGUI itemGUI) {
        this.itemGUI = itemGUI;
    }
}
