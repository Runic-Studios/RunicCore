package com.runicrealms.plugin.api;

import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.item.shops.RunicItemShop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface ShopAPI {

    /**
     * Abstraction of purchase logic that can be used by any shop-type feature
     *
     * @param player          attempting to purchase item
     * @param requiredItems   a map of required items and their amounts
     * @param itemDisplayName the display name of the purchased item
     * @param removePayment   true if payment should be removed
     * @return true if transaction is successful
     */
    boolean checkItemRequirement(Player player, List<Pair<String, Integer>> requiredItems,
                                 String itemDisplayName, boolean removePayment);

    /**
     * Method to match a string to a runic item
     *
     * @param templateID of the currency item
     * @return RunicItem w/ template ID matching string name
     */
    ItemStack getRunicItemCurrency(String templateID);

    /**
     * Check if a player has ALL required items (for quest / shops)
     *
     * @param player        to check
     * @param requiredItems a list of pairs of item TEMPLATE IDS and their required amounts
     * @return true if player has all needed items
     */
    boolean hasAllReqItems(Player player, List<Pair<String, Integer>> requiredItems);

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
     * Registers a RunicItemShop in our in-memory collection
     *
     * @param shop to register
     */
    void registerRunicItemShop(RunicItemShop shop);
}
