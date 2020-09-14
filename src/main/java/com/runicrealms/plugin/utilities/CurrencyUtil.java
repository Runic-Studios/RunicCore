package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.LoreGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * This utility contains all of the units of currency.
 */
public class CurrencyUtil {

    /*
     * A single gold coin, the smallest unit of currency
     */
    public static ItemStack goldCoin() {
        ItemStack coin = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = coin.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Currency of Alterra");
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Gold Coin");
            meta.setLore(lore);
            coin.setItemMeta(meta);
        }
        return coin;
    }

    public static ItemStack goldCoin(int stackSize) {
        ItemStack coin = new ItemStack(Material.GOLD_NUGGET, stackSize);
        ItemMeta meta = coin.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Currency of Alterra");
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Gold Coin");
            meta.setLore(lore);
            coin.setItemMeta(meta);
        }
        return coin;
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
