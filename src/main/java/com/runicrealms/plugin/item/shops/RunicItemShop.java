package com.runicrealms.plugin.item.shops;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

public interface RunicItemShop {

    /*
     * Represents the buyable items in the shop GUI
     * Map key represents the item slot
     */
    Map<Integer, RunicShopItem> getContents();

    /*
     * Amount of slots in the shop GUI
     * NOTICE: does not include the first row of items, occupied by the shop icon
     */
    int getShopSize();

    /*
     * The shop icon that appears in the first row of the GUI
     */
    ItemStack getIcon();

    /*
     * List of NPCs that can open this GUI
     * Uses RunicNpcs, no citizens
     */
    Collection<Integer> getNpcIds();

    /*
     * Shop GUI name
     */
    String getName();
}

