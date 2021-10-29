package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class RunicShopItem {

    private final int price;
    private final String currencyTemplateID;
    private final ItemStack shopItem;
    private final String priceDisplayString;
    private final RunicItemRunnable runicItemRunnable;
    private boolean removePayment = true;

    /**
     * Creates a 'buy item' for the shop!
     * @param price in chosen currency of item
     * @param currencyTemplateID String name of template ID for RunicItem currency
     * @param shopItem to be purchased
     */
    public RunicShopItem(int price, String currencyTemplateID, ItemStack shopItem) {
        this.price = price;
        this.currencyTemplateID = currencyTemplateID;
        this.shopItem = shopItem;
        this.priceDisplayString = CurrencyUtil.goldCoin().getItemMeta().getDisplayName();
        this.runicItemRunnable = runDefaultBuy();
    }

    /**
     * Creates a 'buy item' for the shop!
     *
     * @param price in chosen currency of item
     * @param currencyTemplateID String name of template ID for RunicItem currency
     * @param shopItem to be purchased
     * @param priceDisplayString a string that represents the kind of price (coin, hunter points, etc.)
     */
    public RunicShopItem(int price, String currencyTemplateID, ItemStack shopItem, String priceDisplayString) {
        this.price = price;
        this.currencyTemplateID = currencyTemplateID;
        this.shopItem = shopItem;
        this.priceDisplayString = priceDisplayString;
        this.runicItemRunnable = runDefaultBuy();
    }

    /**
     * Creates a 'buy item' for the shop!
     * @param price in chosen currency of item
     * @param currencyTemplateID String name of template ID for RunicItem currency
     * @param shopItem to be purchased
     * @param runicItemRunnable a custom runnable to be executed upon item purchase
     */
    public RunicShopItem(int price, String currencyTemplateID, ItemStack shopItem, RunicItemRunnable runicItemRunnable) {
        this.price = price;
        this.currencyTemplateID = currencyTemplateID;
        this.shopItem = shopItem;
        this.priceDisplayString = CurrencyUtil.goldCoin().getItemMeta().getDisplayName();
        this.runicItemRunnable = runicItemRunnable;
    }

    /**
     * Creates a 'buy item' for the shop!
     *
     * @param price in chosen currency of item
     * @param currencyTemplateID String name of template ID for RunicItem currency
     * @param shopItem to be purchased
     * @param priceDisplayString a string that represents the kind of price (coin, hunter points, etc.)
     * @param runicItemRunnable a custom runnable to be executed upon item purchase
     */
    public RunicShopItem(int price, String currencyTemplateID, ItemStack shopItem, String priceDisplayString, RunicItemRunnable runicItemRunnable) {
        this.price = price;
        this.currencyTemplateID = currencyTemplateID;
        this.shopItem = shopItem;
        this.priceDisplayString = priceDisplayString;
        this.runicItemRunnable = runicItemRunnable;
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

    public ItemStack getShopItem() {
        return shopItem;
    }

    public String getPriceDisplayString() {
        return priceDisplayString;
    }

    /*
    MAKE SURE somewhere that there is space in the player's inventory.
     */
    public void runBuy(Player player) {
        runicItemRunnable.run(player);
    }

    private RunicItemRunnable runDefaultBuy() {
        return player -> {
            // attempt to give player item (does not drop on floor)
            RunicItemsAPI.addItem(player.getInventory(), shopItem, true);
        };
    }

    public boolean removePayment() {
        return removePayment;
    }

    public void setRemovePayment(boolean removePayment) {
        this.removePayment = removePayment;
    }

    /**
     * The generic item shop lore generator to be used if coins are the price
     *
     * @param is itemstack to be sold
     * @param price of the item
     * @return a display-able item with lore like price info
     */
    public static ItemStack iconWithLore(ItemStack is, int price, String priceItemDisplayName) {
        ItemStack iconWithLore = is.clone();
        ItemMeta meta = iconWithLore.getItemMeta();
        if (meta != null && meta.getLore() != null) {
            List<String> lore = meta.getLore();
            lore.add("");
            lore.add(
                    ChatColor.GOLD + "Price: " +
                            ChatColor.GREEN + ChatColor.BOLD +
                            price + " " + priceItemDisplayName
            );
            meta.setLore(lore);
            iconWithLore.setItemMeta(meta);
        }
        return iconWithLore;
    }
}
