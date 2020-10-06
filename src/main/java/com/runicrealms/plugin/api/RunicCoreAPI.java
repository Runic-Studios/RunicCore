package com.runicrealms.plugin.api;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.shops.RunicItemShop;
import com.runicrealms.plugin.item.shops.RunicShopManager;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.player.combat.CombatListener;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RunicCoreAPI {

    public static int getBaseOutlawRating() {
        return RunicCore.getBaseOutlawRating();
    }

    /**
     * Gets the MythicMobs item w/ internal name matching string
     * @param itemName internal name of item (NOT DISPLAY NAME)
     * @param amount of itemstack
     * @return an ItemStack
     */
    public static ItemStack getMythicItem(String itemName, int amount) {
        MythicItem mi = MythicMobs.inst().getItemManager().getItem(itemName).get();
        AbstractItemStack abstractItemStack = mi.generateItemStack(amount);
        return BukkitAdapter.adapt(abstractItemStack);
    }

    /**
     * Gets the MythicMobs item w/ internal name matching string, randomizes stack size
     * @param internalName internal name of item (NOT DISPLAY NAME)
     * @param rand some Random object
     * @param minStackSize minimum size of stack
     * @param maxStackSize max size of stack
     * @return an ItemStack
     */
    public static ItemStack getMythicItem(String internalName, Random rand, int minStackSize, int maxStackSize) {
        MythicItem mi = MythicMobs.inst().getItemManager().getItem(internalName).get();
        AbstractItemStack abstractItemStack = mi.generateItemStack(rand.nextInt(maxStackSize - minStackSize) + minStackSize);
        return BukkitAdapter.adapt(abstractItemStack);
    }

    public static PlayerCache getPlayerCache(Player player) {
        return RunicCore.getCacheManager().getPlayerCaches().get(player);
    }

    public static void tagCombat(Player damager, Entity victim) {
        CombatListener.tagCombat(damager, victim);
    }

    public static void registerRunicItemShop(RunicItemShop shop) {
        RunicShopManager.registerShop(shop);
    }
}
