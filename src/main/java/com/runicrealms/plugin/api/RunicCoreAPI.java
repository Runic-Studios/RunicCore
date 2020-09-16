package com.runicrealms.plugin.api;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.shops.RunicItemShop;
import com.runicrealms.plugin.item.shops.RunicShopManager;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.player.outlaw.OutlawManager;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

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

    /*
    Gets a list of the outlaw ratings of online players, sorted high --> low
     */
    public static Map<Player, Integer> getOutlawRatings() {
        return OutlawManager.getOutlawRatings();
    }

    public static PlayerCache getPlayerCache(Player player) {
        return RunicCore.getCacheManager().getPlayerCaches().get(player);
    }

    public static void registerRunicItemShop(RunicItemShop shop) {
        RunicShopManager.registerShop(shop);
    }
}
