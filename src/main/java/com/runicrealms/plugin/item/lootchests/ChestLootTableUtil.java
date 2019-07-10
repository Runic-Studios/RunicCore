package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.item.commands.ItemCMD;
import com.runicrealms.plugin.professions.gathering.GatheringUtil;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * A util to retrieve weighted loot tables for each tier of loot chest.
 */
public class ChestLootTableUtil {

    public static WeightedRandomBag<ItemStack> commonLootTable() {

        // create a loot table object
        WeightedRandomBag<ItemStack> commonLootTable = new WeightedRandomBag<>();

        // add the gear chance
        ItemStack commonItem = ItemCMD.generateCommonItem();

        // currency
        Random rand = new Random();
        //int coinStackSize = rand.nextInt(12) + 1;
        ItemStack coin = CurrencyUtil.goldCoin(rand.nextInt(6 - 3) + 3);

        // food
        ItemStack bread = mythicItem("Bread", rand, 4);

        // materials
        //int materialStackSize = rand.nextInt(3) + 1;
        MythicItem sw = MythicMobs.inst().getItemManager().getItem("SpruceWood").get();
        AbstractItemStack abstractSpruce = sw.generateItemStack(rand.nextInt(5) + 1);
        ItemStack spruceWood = BukkitAdapter.adapt(abstractSpruce);
        MythicItem thr = MythicMobs.inst().getItemManager().getItem("Thread").get();
        AbstractItemStack abstractThread = thr.generateItemStack(rand.nextInt(3) + 1);
        ItemStack thread = BukkitAdapter.adapt(abstractThread);
        MythicItem ah = MythicMobs.inst().getItemManager().getItem("AnimalHide").get();
        AbstractItemStack abstractHide = ah.generateItemStack(rand.nextInt(3) + 1);
        ItemStack animalHide = BukkitAdapter.adapt(abstractHide);
//        MythicItem mi = MythicMobs.inst().getItemManager().getItem("OakWood").get();
//        AbstractItemStack abstractBread = mi.generateItemStack(materialStackSize);
//        ItemStack bread = BukkitAdapter.adapt(abstractBread);
//        MythicItem mi = MythicMobs.inst().getItemManager().getItem("UncutRuby").get();
//        AbstractItemStack abstractBread = mi.generateItemStack(materialStackSize);
//        ItemStack bread = BukkitAdapter.adapt(abstractBread);
//        MythicItem mi = MythicMobs.inst().getItemManager().getItem("Bottle").get();
//        AbstractItemStack abstractBread = mi.generateItemStack(materialStackSize);
//        ItemStack bread = BukkitAdapter.adapt(abstractBread);
//        MythicItem mi = MythicMobs.inst().getItemManager().getItem("Salmon").get();
//        AbstractItemStack abstractBread = mi.generateItemStack(materialStackSize);
//        ItemStack bread = BukkitAdapter.adapt(abstractBread);

        // gatherting tools (tier 1)
        ItemStack gatheringAxe = GatheringUtil.getGatheringTool(Material.IRON_AXE, 1);
        ItemStack gathertingHoe = GatheringUtil.getGatheringTool(Material.IRON_HOE, 1);
        ItemStack gatheringPick = GatheringUtil.getGatheringTool(Material.IRON_PICKAXE, 1);
        ItemStack gatheringRod = GatheringUtil.getGatheringTool(Material.FISHING_ROD, 1);

        // potions
        ItemStack healthPotion = ItemCMD.generatePotion("healing", 20);
        ItemStack manaPotion = ItemCMD.generatePotion("mana", 25);

        // add entries to table
        commonLootTable.addEntry(commonItem,  50.0);
        commonLootTable.addEntry(coin, 35.0);
        commonLootTable.addEntry(bread, 50.0);

        commonLootTable.addEntry(spruceWood, 8.0);
        commonLootTable.addEntry(thread, 8.0);
        commonLootTable.addEntry(animalHide, 8.0);

        commonLootTable.addEntry(gatheringAxe, 4.0);
        commonLootTable.addEntry(gathertingHoe, 4.0);
        commonLootTable.addEntry(gatheringPick, 4.0);
        commonLootTable.addEntry(gatheringRod, 6.0);

        commonLootTable.addEntry(healthPotion, 12.0);
        commonLootTable.addEntry(manaPotion, 12.0);

        return commonLootTable;
    }

    private static ItemStack mythicItem(String name, Random rand, int maxStackSize) {
        MythicItem mi = MythicMobs.inst().getItemManager().getItem(name).get();
        AbstractItemStack abstractItemStack = mi.generateItemStack(rand.nextInt(maxStackSize) + 1);
        ItemStack itemStack = BukkitAdapter.adapt(abstractItemStack);
        return itemStack;
    }
}
