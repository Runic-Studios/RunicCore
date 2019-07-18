package com.runicrealms.plugin.mysterybox;

import com.runicrealms.plugin.item.lootchests.WeightedRandomBag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by KissOfFate
 * Date: 7/17/2019
 * Time: 8:05 PM
 */
public class MysteryLoot {
    private static List<MysteryItem> mysteryItems = new ArrayList<>();

    static {
        loadItems();
    }

    /**
     * Just load items here for simplicity
     */
    private static void loadItems() {
        mysteryItems.add(new MysteryItem(new ItemStack(Material.GOLD_NUGGET), "lp user {player} permission set runic.item", 0.1));

        Bukkit.getLogger().log(Level.INFO, "Loaded " + mysteryItems.size() + " mystery box items!");
    }

    public static WeightedRandomBag<MysteryItem> getMysteryTable() {
        // create a loot table object
        WeightedRandomBag<MysteryItem> mysteryTable = new WeightedRandomBag<>();

        for(MysteryItem item : mysteryItems) {
            mysteryTable.addEntry(item, item.getWeight());
        }

        return mysteryTable;
    }

    public static List<MysteryItem> getMysteryItems() {
        return mysteryItems;
    }
}
