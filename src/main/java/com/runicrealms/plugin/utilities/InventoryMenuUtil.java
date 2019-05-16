package com.runicrealms.plugin.utilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Used to create inventory GUIs.
 */
public class InventoryMenuUtil {

    /**
     * @param pl - 'owner' of the inventory
     * @param size - size of the inventory. must be an interval of 9.
     * @param name - name of the inventory.
     */
    public static Inventory invMenu(Player pl, int size, String name) {

        Inventory inv = Bukkit.createInventory(pl, size, name);
        return inv;
    }
}
