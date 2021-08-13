package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;

public class RunicShopFactory {

    private final ItemStack healingPotion = RunicItemsAPI.generateItemFromTemplate("minor-crafted-potion-healing").generateItem();

    public RunicShopFactory() {
        getAlchemistShop();
    }

    public RunicShopGeneric getAlchemistShop() {
        HashMap<ItemStack, RunicShopItem> shopItems = new HashMap<>();
        shopItems.put(healingPotion, new RunicShopItem(8, "Coin", RunicShopGeneric.iconWithLore(healingPotion, 8, CurrencyUtil.goldCoin().getItemMeta().getDisplayName())));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Alchemist", Collections.singletonList(101), shopItems);
    }
}
