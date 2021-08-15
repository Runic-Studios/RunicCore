package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

public class RunicShopFactory {

    public RunicShopFactory() {
        getAlchemistShop();
        getGeneralStore();
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
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Alchemist", Arrays.asList(101, 103, 104, 105, 106, 107, 108, 109, 110, 111), shopItems);
    }

    private final ItemStack beetroot = RunicItemsAPI.generateItemFromTemplate("azanashop-beetroot").generateItem();
    private final ItemStack carrot = RunicItemsAPI.generateItemFromTemplate("azanashop-carrot").generateItem();
    private final ItemStack azanaShopArcherBow = RunicItemsAPI.generateItemFromTemplate("azanashop-archer-bow").generateItem();
    private final ItemStack azanaShopClericMace = RunicItemsAPI.generateItemFromTemplate("azanashop-cleric-mace").generateItem();
    private final ItemStack azanaShopMageStaff = RunicItemsAPI.generateItemFromTemplate("azanashop-mage-staff").generateItem();
    private final ItemStack azanaShopRogueSword = RunicItemsAPI.generateItemFromTemplate("azanashop-rogue-sword").generateItem();
    private final ItemStack azanaShopWarriorAxe = RunicItemsAPI.generateItemFromTemplate("azanashop-warrior-axe").generateItem();
    private final ItemStack azanaShopArcherBoots = RunicItemsAPI.generateItemFromTemplate("azanashop-archer-boots").generateItem();
    private final ItemStack azanaShopClericHelmet = RunicItemsAPI.generateItemFromTemplate("azanashop-cleric-helmet").generateItem();
    private final ItemStack azanaShopMageHelmet = RunicItemsAPI.generateItemFromTemplate("azanashop-mage-helmet").generateItem();
    private final ItemStack azanaShopRogueBoots = RunicItemsAPI.generateItemFromTemplate("azanashop-rogue-boots").generateItem();
    private final ItemStack azanaShopWarriorHelmet = RunicItemsAPI.generateItemFromTemplate("azanashop-warrior-helmet").generateItem();

    public RunicShopGeneric getGeneralStore() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put(beetroot, new RunicShopItem(2, "Coin", RunicShopGeneric.iconWithLore(beetroot, 2, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(carrot, new RunicShopItem(3, "Coin", RunicShopGeneric.iconWithLore(carrot, 3, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(minorHealingPotion, new RunicShopItem(10, "Coin", RunicShopGeneric.iconWithLore(minorHealingPotion, 8, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(minorManaPotion, new RunicShopItem(10, "Coin", RunicShopGeneric.iconWithLore(minorManaPotion, 8, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopArcherBow, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopArcherBow, 12, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopClericMace, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopClericMace, 12, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopMageStaff, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopMageStaff, 12, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopRogueSword, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopRogueSword, 12, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopWarriorAxe, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopWarriorAxe, 12, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopArcherBoots, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopArcherBoots, 9, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopClericHelmet, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopClericHelmet, 9, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopMageHelmet, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopMageHelmet, 9, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopRogueBoots, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopRogueBoots, 9, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        shopItems.put(azanaShopWarriorHelmet, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopWarriorHelmet, 9, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        return new RunicShopGeneric(45, ChatColor.YELLOW + "Alchemist", Collections.singletonList(102), shopItems, new int[]{0, 1, 2, 3, 9, 10, 11, 12, 13, 18, 19, 20, 21, 22});
    }
}
