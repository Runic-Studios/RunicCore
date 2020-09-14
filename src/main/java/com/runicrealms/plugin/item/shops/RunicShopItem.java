package com.runicrealms.plugin.item.shops;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RunicShopItem {

    private final int price;
    private final String mythicCurrencyName;
    private final ItemStack item;
    private final RunicItemRunnable runicItemRunnable;
    private boolean removePayment = true;

    /**
     * Creates a 'buy item' for the shop!
     * @param price in chosen currency of item
     * @param mythicCurrencyName String name of mythic item used as price
     * @param item to be purchased
     */
    public RunicShopItem(int price, String mythicCurrencyName, ItemStack item, RunicItemRunnable runicItemRunnable) {
        this.price = price;
        this.mythicCurrencyName = mythicCurrencyName;
        this.item = item;
        this.runicItemRunnable = runicItemRunnable;
    }

    public int getPrice() {
        return price;
    }

    /**
     * Method to match a string to a mythic item
     * @return MythicItem matching string name
     */
    public ItemStack getMythicCurrency() {
        MythicItem mi = MythicMobs.inst().getItemManager().getItem(mythicCurrencyName).get();
        AbstractItemStack abstractItemStack = mi.generateItemStack(price);
        return BukkitAdapter.adapt(abstractItemStack);
    }

    public ItemStack getItem() {
        return item;
    }

    /*
    MAKE SURE somewhere that there is space in the player's inventory.
     */
    public void runBuy(Player player) {
        runicItemRunnable.run(player);
    }

    public boolean removePayment() {
        return removePayment;
    }

    public void setRemovePayment(boolean removePayment) {
        this.removePayment = removePayment;
    }
}
