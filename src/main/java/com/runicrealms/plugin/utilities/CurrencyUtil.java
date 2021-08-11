package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * This utility contains all the units of currency.
 */
public class CurrencyUtil {

    private static final ItemStack GOLD_COIN = RunicItemsAPI.generateItemFromTemplate("Coin").generateItem();

    /*
     * A single gold coin, the smallest unit of currency
     */
    public static ItemStack goldCoin() {
        return GOLD_COIN;
    }

    public static ItemStack goldCoin(int stackSize) {
        return RunicItemsAPI.generateItemFromTemplate("Coin", stackSize).generateItem();
    }

    /**
     *
     * @param size how many coins the pouch can hold
     * @return GoldPouch ItemStack
     */
    public static ItemStack goldPouch(int size) {
        ItemStack goldPouch = new ItemStack(Material.SHEARS);
        goldPouch = AttributeUtil.addCustomStat(goldPouch, "pouchSize", size);
        LoreGenerator.generateGoldPouchLore(goldPouch);
        return goldPouch;
    }
}
