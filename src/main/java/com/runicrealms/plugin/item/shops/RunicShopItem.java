package com.runicrealms.plugin.item.shops;

import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RunicShopItem {

    private final int price;
    private final String currencyTemplateID;
    private final ItemStack shopIcon;
    private final RunicItemRunnable runicItemRunnable;
    private boolean removePayment = true;

    /**
     * Creates a 'buy item' for the shop!
     * @param price in chosen currency of item
     * @param currencyTemplateID String name of template ID for RunicItem currency
     * @param shopIcon to be purchased
     */
    public RunicShopItem(int price, String currencyTemplateID, ItemStack shopIcon, RunicItemRunnable runicItemRunnable) {
        this.price = price;
        this.currencyTemplateID = currencyTemplateID;
        this.shopIcon = shopIcon;
        this.runicItemRunnable = runicItemRunnable;
    }

    /**
     * Creates a 'buy item' for the shop!
     * @param price in chosen currency of item
     * @param currencyTemplateID String name of template ID for RunicItem currency
     * @param shopIcon to be purchased
     */
    public RunicShopItem(int price, String currencyTemplateID, ItemStack shopIcon) {
        this.price = price;
        this.currencyTemplateID = currencyTemplateID;
        this.shopIcon = shopIcon;
        this.runicItemRunnable = runDefaultBuy(shopIcon);
    }

    public int getPrice() {
        return price;
    }

    /**
     * Method to match a string to a runic item
     * @return RunicItem w/ template ID matching string name
     */
    public ItemStack getRunicItemCurrency() {
        return RunicItemsAPI.generateItemFromTemplate(currencyTemplateID).generateItem();
    }

    public ItemStack getShopIcon() {
        return shopIcon;
    }

    /*
    MAKE SURE somewhere that there is space in the player's inventory.
     */
    public void runBuy(Player player) {
        runicItemRunnable.run(player);
    }

    private RunicItemRunnable runDefaultBuy(ItemStack item) {
        return player -> {
            // attempt to give player item (does not drop on floor)
            player.getInventory().addItem(item);
        };
    }

    public boolean removePayment() {
        return removePayment;
    }

    public void setRemovePayment(boolean removePayment) {
        this.removePayment = removePayment;
    }
}
