package com.runicrealms.plugin.api;

import com.runicrealms.plugin.item.shops.RunicItemShop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ShopAPI {

    /**
     * Check if a player has required items (for quest or shops)
     *
     * @param player    to check
     * @param itemStack to check
     * @param needed    how many items do they need
     * @return true if player has the items
     */
    boolean hasItems(Player player, ItemStack itemStack, int needed);

    /**
     * Registers a RunicItemShop in our in-memory collection
     *
     * @param shop to register
     */
    void registerRunicItemShop(RunicItemShop shop);
}
