package com.runicrealms.plugin.api;

import com.runicrealms.plugin.item.shops.RunicItemShop;
import com.runicrealms.plugin.item.shops.RunicShopManager;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.inventory.ItemStack;

public class RunicCoreAPI {

    /**
     * Gets the MythicMobs item w/ internal name matching string
     * @param itemName internal name of item
     * @param amount of itemstack
     * @return an ItemStack
     */
    public static ItemStack getMythicItem(String itemName, int amount) {
        MythicItem mi = MythicMobs.inst().getItemManager().getItem(itemName).get();
        AbstractItemStack abstractItemStack = mi.generateItemStack(amount);
        return BukkitAdapter.adapt(abstractItemStack);
    }

    public static void registerRunicItemShop(RunicItemShop shop) {
        RunicShopManager.registerShop(shop);
    }
}
