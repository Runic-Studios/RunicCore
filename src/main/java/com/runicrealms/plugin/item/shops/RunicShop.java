package com.runicrealms.plugin.item.shops;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/**
 * All shops which require custom or unusual mechanics (item scrapper, jewel master etc.) implement this interface.
 * This is different from RunicItemShops, which sell items and are more straightforward in functionality.
 */
public interface RunicShop {

    int getShopSize();

    ItemStack getIcon();

    String getName();

    Collection<Integer> getRunicNpcIds();

    InventoryHolder getInventoryHolder();
}
