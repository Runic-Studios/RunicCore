package com.runicrealms.plugin.api;

import com.runicrealms.plugin.item.shops.RunicItemShop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface ShopAPI {

    /**
     * Check if a player has required item (for quest or shops)
     *
     * @param player    to check
     * @param itemStack to check
     * @param needed    how many items do they need
     * @return true if player has the item
     */
    boolean hasItem(Player player, ItemStack itemStack, int needed);

    /**
     * Check if a player has ALL required items (for quest / shops)
     *
     * @param player   to check
     * @param reqItems a map of item TEMPLATE IDS and their required amounts
     * @return true if player has all needed items
     */
    boolean hasAllReqItems(Player player, Map<String, Integer> reqItems);

    /**
     * Registers a RunicItemShop in our in-memory collection
     *
     * @param shop to register
     */
    void registerRunicItemShop(RunicItemShop shop);
}
