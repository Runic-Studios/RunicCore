package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.artifact.utilities.ItemUtil;
import com.runicrealms.plugin.item.util.ItemUtils;
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

        Random rand = new Random();

        // create a loot table object
        WeightedRandomBag<ItemStack> commonLootTable = new WeightedRandomBag<>();

        // add the gear chance
        ItemStack commonArmor = ItemUtils.generateCommonArmor();
        ItemStack commonWeapon = ItemUtil.generateCommonWeapon();

        // currency
        ItemStack coin = CurrencyUtil.goldCoin(rand.nextInt(6 - 4) + 4);

        // food
        ItemStack bread = mythicItem("Bread", rand, 2, 4);

        // materials
        ItemStack spruceWood = mythicItem("SpruceWood", rand, 3, 5);
        ItemStack oakWood = mythicItem("OakWood", rand, 3, 5);
        ItemStack thread = mythicItem("Thread", rand, 3, 5);
        ItemStack animalHide = mythicItem("AnimalHide", rand, 3, 5);
        ItemStack uncutRuby = mythicItem("UncutRuby", rand, 2, 3);
        ItemStack bottle = mythicItem("Bottle", rand, 2, 3);
        ItemStack salmon = mythicItem("Salmon", rand, 2, 3);

        // gatherting tools (tier 1)
        ItemStack gatheringAxe = GatheringUtil.getGatheringTool(Material.IRON_AXE, 1);
        ItemStack gathertingHoe = GatheringUtil.getGatheringTool(Material.IRON_HOE, 1);
        ItemStack gatheringPick = GatheringUtil.getGatheringTool(Material.IRON_PICKAXE, 1);
        ItemStack gatheringRod = GatheringUtil.getGatheringTool(Material.FISHING_ROD, 1);

        // potions
        ItemStack healthPotion = ItemUtils.generatePotion("healing", 25);
        ItemStack manaPotion = ItemUtils.generatePotion("mana", 25);

        // add entries to table
        commonLootTable.addEntry(commonArmor,  35.0);
        commonLootTable.addEntry(commonWeapon,  35.0);
        commonLootTable.addEntry(coin, 50.0);
        commonLootTable.addEntry(bread, 50.0);

        commonLootTable.addEntry(spruceWood, 8.0);
        commonLootTable.addEntry(oakWood, 8.0);
        commonLootTable.addEntry(thread, 8.0);
        commonLootTable.addEntry(animalHide, 8.0);
        commonLootTable.addEntry(uncutRuby, 8.0);
        commonLootTable.addEntry(bottle, 8.0);
        commonLootTable.addEntry(salmon, 8.0);

        commonLootTable.addEntry(gatheringAxe, 4.0);
        commonLootTable.addEntry(gathertingHoe, 4.0);
        commonLootTable.addEntry(gatheringPick, 4.0);
        commonLootTable.addEntry(gatheringRod, 6.0);

        commonLootTable.addEntry(healthPotion, 12.0);
        commonLootTable.addEntry(manaPotion, 12.0);

        return commonLootTable;
    }

    public static WeightedRandomBag<ItemStack> uncommonLootTable() {

        Random rand = new Random();

        // create a loot table object
        WeightedRandomBag<ItemStack> uncommonLootTable = new WeightedRandomBag<>();

        // add the gear chance
        ItemStack uncommonArmor = ItemUtils.generateUncommonArmor();
        ItemStack uncommonWeapon = ItemUtil.generateUncommonWeapon();

        // currency
        ItemStack coin = CurrencyUtil.goldCoin(rand.nextInt(8 - 4) + 4);

        // food
        ItemStack bread = mythicItem("Bread", rand, 2, 4);

        // materials
        ItemStack spruceWood = mythicItem("SpruceWood", rand, 3, 5);
        ItemStack oakWood = mythicItem("OakWood", rand, 3, 5);
        ItemStack thread = mythicItem("Thread", rand, 3, 5);
        ItemStack animalHide = mythicItem("AnimalHide", rand, 3, 5);
        ItemStack uncutRuby = mythicItem("UncutRuby", rand, 2, 3);
        ItemStack uncutSapphire = mythicItem("UncutSapphire", rand, 2, 3);
        ItemStack bottle = mythicItem("Bottle", rand, 3, 5);
        ItemStack cod = mythicItem("Cod", rand, 2, 3);

        // gatherting tools (tier 2)
        ItemStack gatheringAxe = GatheringUtil.getGatheringTool(Material.IRON_AXE, 2);
        ItemStack gathertingHoe = GatheringUtil.getGatheringTool(Material.IRON_HOE, 2);
        ItemStack gatheringPick = GatheringUtil.getGatheringTool(Material.IRON_PICKAXE, 2);
        ItemStack gatheringRod = GatheringUtil.getGatheringTool(Material.FISHING_ROD, 2);

        // potions
        ItemStack healthPotion = ItemUtils.generatePotion("healing", 25);
        ItemStack manaPotion = ItemUtils.generatePotion("mana", 25);

        // add entries to table
        uncommonLootTable.addEntry(uncommonArmor,  25.0);
        uncommonLootTable.addEntry(uncommonWeapon,  25.0);

        uncommonLootTable.addEntry(coin, 50.0);
        uncommonLootTable.addEntry(bread, 35.0);

        uncommonLootTable.addEntry(spruceWood, 8.0);
        uncommonLootTable.addEntry(oakWood, 8.0);
        uncommonLootTable.addEntry(thread, 8.0);
        uncommonLootTable.addEntry(animalHide, 8.0);
        uncommonLootTable.addEntry(uncutRuby, 8.0);
        uncommonLootTable.addEntry(uncutSapphire, 8.0);
        uncommonLootTable.addEntry(bottle, 8.0);
        uncommonLootTable.addEntry(cod, 8.0);

        uncommonLootTable.addEntry(gatheringAxe, 3.0);
        uncommonLootTable.addEntry(gathertingHoe, 3.0);
        uncommonLootTable.addEntry(gatheringPick, 3.0);
        uncommonLootTable.addEntry(gatheringRod, 5.0);

        uncommonLootTable.addEntry(healthPotion, 15.0);
        uncommonLootTable.addEntry(manaPotion, 15.0);

        return uncommonLootTable;
    }

    public static WeightedRandomBag<ItemStack> rareLootTable() {

        Random rand = new Random();

        // create a loot table object
        WeightedRandomBag<ItemStack> rareLootTable = new WeightedRandomBag<>();

        // add the gear chance
        ItemStack rareArmor = ItemUtils.generateRareArmor();
        ItemStack rareWeapon = ItemUtil.generateRareWeapon();

        // currency
        ItemStack coin = CurrencyUtil.goldCoin(rand.nextInt(10 - 5) + 5);

        // food
        ItemStack bread = mythicItem("Bread", rand, 2, 4);

        // crafting materials
        ItemStack spruceWood = mythicItem("SpruceWood", rand, 3, 5);
        ItemStack oakWood = mythicItem("OakWood", rand, 3, 5);
        ItemStack thread = mythicItem("Thread", rand, 3, 5);
        ItemStack animalHide = mythicItem("AnimalHide", rand, 3, 5);
        ItemStack uncutRuby = mythicItem("UncutRuby", rand, 2, 3);
        ItemStack uncutSapphire = mythicItem("UncutSapphire", rand, 2, 3);
        ItemStack uncutOpal = mythicItem("UncutOpal", rand, 2, 3);
        ItemStack bottle = mythicItem("Bottle", rand, 3, 5);
        ItemStack tropical = mythicItem("Tropical", rand, 2, 3);

        // gatherting tools (tier 3)
        ItemStack gatheringAxe = GatheringUtil.getGatheringTool(Material.IRON_AXE, 3);
        ItemStack gathertingHoe = GatheringUtil.getGatheringTool(Material.IRON_HOE, 3);
        ItemStack gatheringPick = GatheringUtil.getGatheringTool(Material.IRON_PICKAXE, 3);
        ItemStack gatheringRod = GatheringUtil.getGatheringTool(Material.FISHING_ROD, 3);

        // potions
        ItemStack healthPotion = ItemUtils.generatePotion("healing", 40);
        ItemStack manaPotion = ItemUtils.generatePotion("mana", 40);

        // add entries to table
        rareLootTable.addEntry(rareArmor,  25.0);
        rareLootTable.addEntry(rareWeapon,  25.0);
        rareLootTable.addEntry(coin, 50.0);
        rareLootTable.addEntry(bread, 35.0);

        rareLootTable.addEntry(spruceWood, 8.0);
        rareLootTable.addEntry(oakWood, 8.0);
        rareLootTable.addEntry(thread, 8.0);
        rareLootTable.addEntry(animalHide, 8.0);
        rareLootTable.addEntry(uncutRuby, 8.0);
        rareLootTable.addEntry(uncutSapphire, 8.0);
        rareLootTable.addEntry(uncutOpal, 8.0);
        rareLootTable.addEntry(bottle, 8.0);
        rareLootTable.addEntry(tropical, 8.0);

        rareLootTable.addEntry(gatheringAxe, 2.0);
        rareLootTable.addEntry(gathertingHoe, 2.0);
        rareLootTable.addEntry(gatheringPick, 2.0);
        rareLootTable.addEntry(gatheringRod, 4.0);

        rareLootTable.addEntry(healthPotion, 15.0);
        rareLootTable.addEntry(manaPotion, 15.0);

        return rareLootTable;
    }

    public static WeightedRandomBag<ItemStack> epicLootTable() {

        Random rand = new Random();

        // create a loot table object
        WeightedRandomBag<ItemStack> epicLootTable = new WeightedRandomBag<>();

        // add the gear chance
        ItemStack epicArmor = ItemUtils.generateEpicArmor();
        ItemStack epicWeapon = ItemUtil.generateEpicWeapon();

        // currency
        ItemStack coin = CurrencyUtil.goldCoin(rand.nextInt(10 - 5) + 5);

        // food
        ItemStack bread = mythicItem("Bread", rand, 2, 4);

        // materials
        ItemStack spruceWood = mythicItem("SpruceWood", rand, 3, 5);
        ItemStack oakWood = mythicItem("OakWood", rand, 3, 5);
        ItemStack thread = mythicItem("Thread", rand, 3, 5);
        ItemStack animalHide = mythicItem("AnimalHide", rand, 3, 5);
        ItemStack uncutRuby = mythicItem("UncutRuby", rand, 2, 3);
        ItemStack uncutSapphire = mythicItem("UncutSapphire", rand, 2, 3);
        ItemStack uncutOpal = mythicItem("UncutOpal", rand, 2, 3);
        ItemStack uncutEmerald = mythicItem("UncutEmerald", rand, 2, 3);
        ItemStack uncutDiamond = mythicItem("UncutDiamond", rand, 2, 3);
        ItemStack bottle = mythicItem("Bottle", rand, 3, 5);
        ItemStack pufferfish = mythicItem("Pufferfish", rand, 2, 3);

        // gatherting tools (tier 4)
        ItemStack gatheringAxe = GatheringUtil.getGatheringTool(Material.IRON_AXE, 4);
        ItemStack gathertingHoe = GatheringUtil.getGatheringTool(Material.IRON_HOE, 4);
        ItemStack gatheringPick = GatheringUtil.getGatheringTool(Material.IRON_PICKAXE, 4);
        ItemStack gatheringRod = GatheringUtil.getGatheringTool(Material.FISHING_ROD, 4);

        // potions
        ItemStack healthPotion = ItemUtils.generatePotion("healing", 60);
        ItemStack manaPotion = ItemUtils.generatePotion("mana", 60);

        // add entries to table
        epicLootTable.addEntry(epicArmor,  15.0);
        epicLootTable.addEntry(epicWeapon,  15.0);
        epicLootTable.addEntry(coin, 50.0);
        epicLootTable.addEntry(bread, 35.0);

        epicLootTable.addEntry(spruceWood, 8.0);
        epicLootTable.addEntry(oakWood, 8.0);
        epicLootTable.addEntry(thread, 8.0);
        epicLootTable.addEntry(animalHide, 8.0);
        epicLootTable.addEntry(uncutRuby, 8.0);
        epicLootTable.addEntry(uncutSapphire, 8.0);
        epicLootTable.addEntry(uncutOpal, 8.0);
        epicLootTable.addEntry(uncutEmerald, 8.0);
        epicLootTable.addEntry(uncutDiamond, 8.0);

        epicLootTable.addEntry(bottle, 8.0);
        epicLootTable.addEntry(pufferfish, 8.0);

        epicLootTable.addEntry(gatheringAxe, 2.0);
        epicLootTable.addEntry(gathertingHoe, 2.0);
        epicLootTable.addEntry(gatheringPick, 2.0);
        epicLootTable.addEntry(gatheringRod, 4.0);

        epicLootTable.addEntry(healthPotion, 15.0);
        epicLootTable.addEntry(manaPotion, 15.0);

        return epicLootTable;
    }

    private static ItemStack mythicItem(String internalName, Random rand, int minStackSize, int maxStackSize) {
        MythicItem mi = MythicMobs.inst().getItemManager().getItem(internalName).get();
        AbstractItemStack abstractItemStack = mi.generateItemStack(rand.nextInt(maxStackSize - minStackSize) + minStackSize);
        return BukkitAdapter.adapt(abstractItemStack);
    }
}
