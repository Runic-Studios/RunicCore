package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;

public class RunicShopFactory {

    public RunicShopFactory() {
        getAlchemistShop();
    }

    private final ItemStack bottle = RunicItemsAPI.generateItemFromTemplate("Bottle").generateItem();
    private final ItemStack minorHealingPotion = RunicItemsAPI.generateItemFromTemplate("minor-potion-healing").generateItem();
    private final ItemStack minorManaPotion = RunicItemsAPI.generateItemFromTemplate("minor-potion-mana").generateItem();
    private final ItemStack majorHealingPotion = RunicItemsAPI.generateItemFromTemplate("major-potion-healing").generateItem();
    private final ItemStack majorManaPotion = RunicItemsAPI.generateItemFromTemplate("major-potion-mana").generateItem();
    private final ItemStack greaterHealingPotion = RunicItemsAPI.generateItemFromTemplate("greater-potion-healing").generateItem();
    private final ItemStack greaterManaPotion = RunicItemsAPI.generateItemFromTemplate("greater-potion-mana").generateItem();

    public RunicShopGeneric getAlchemistShop() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put(bottle, new RunicShopItem(2, "Coin", RunicShopGeneric.iconWithLore(bottle, 2, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(minorHealingPotion, new RunicShopItem(8, "Coin", RunicShopGeneric.iconWithLore(minorHealingPotion, 8, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(minorManaPotion, new RunicShopItem(8, "Coin", RunicShopGeneric.iconWithLore(minorManaPotion, 8, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(majorHealingPotion, new RunicShopItem(16, "Coin", RunicShopGeneric.iconWithLore(majorHealingPotion, 16, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(majorManaPotion, new RunicShopItem(16, "Coin", RunicShopGeneric.iconWithLore(majorManaPotion, 16, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(greaterHealingPotion, new RunicShopItem(24, "Coin", RunicShopGeneric.iconWithLore(greaterHealingPotion, 24, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(greaterManaPotion, new RunicShopItem(24, "Coin", RunicShopGeneric.iconWithLore(greaterManaPotion, 24, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Alchemist", Collections.singletonList(101), shopItems);
    }
}
