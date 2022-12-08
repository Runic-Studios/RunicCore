package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.LootTableAPI;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A util to retrieve weighted loot tables for each tier of loot chest.
 */
public class LootTableManager implements LootTableAPI {

    private static WeightedRandomBag<ItemStack> LOOT_TABLE_TIER_I;

    static {
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            // create a loot table object
            LOOT_TABLE_TIER_I = new WeightedRandomBag<>();

            // armor and weapons
            LootChestTier common = LootChestTier.TIER_I;
            ItemStack randomArmorOrWeaponInLevelRange = RunicItemsAPI.generateItemInRange(common.getMinLootLevel(), common.getMaxLootLevel(), 1).generateItem();

            ItemStack coin = CurrencyUtil.goldCoin(ThreadLocalRandom.current().nextInt(4, 6 + 1)); // bound is not inclusive, so we add 1
            ItemStack bread = runicItem("Bread", 2, 4);

            // materials
            ItemStack spruceWood = runicItem("SpruceWood", 3, 5);
            ItemStack oakWood = runicItem("OakWood", 3, 5);
            ItemStack thread = runicItem("Thread", 3, 5);
            ItemStack animalHide = runicItem("AnimalHide", 3, 5);
            ItemStack uncutRuby = runicItem("uncut-ruby", 2, 3);
            ItemStack bottle = runicItem("Bottle", 2, 3);
            ItemStack salmon = runicItem("Salmon", 2, 3);

            // potions
            ItemStack healthPotion = runicItem("minor-potion-healing", 1, 1);
            ItemStack manaPotion = runicItem("minor-potion-mana", 1, 1);

            // add entries to table
            LOOT_TABLE_TIER_I.addEntry(randomArmorOrWeaponInLevelRange, 70.0);
            LOOT_TABLE_TIER_I.addEntry(coin, 50.0);
            LOOT_TABLE_TIER_I.addEntry(bread, 50.0);

            LOOT_TABLE_TIER_I.addEntry(spruceWood, 8.0);
            LOOT_TABLE_TIER_I.addEntry(oakWood, 8.0);
            LOOT_TABLE_TIER_I.addEntry(thread, 8.0);
            LOOT_TABLE_TIER_I.addEntry(animalHide, 8.0);
            LOOT_TABLE_TIER_I.addEntry(uncutRuby, 8.0);
            LOOT_TABLE_TIER_I.addEntry(bottle, 16.0);
            LOOT_TABLE_TIER_I.addEntry(salmon, 8.0);

            LOOT_TABLE_TIER_I.addEntry(healthPotion, 20.0);
            LOOT_TABLE_TIER_I.addEntry(manaPotion, 20.0);
        }, 10 * 20L);
    }

    public static WeightedRandomBag<ItemStack> lootTableTierII() {

        // create a loot table object
        WeightedRandomBag<ItemStack> uncommonLootTable = new WeightedRandomBag<>();

        // armor and weapons
        LootChestTier uncommon = LootChestTier.TIER_II;
        ItemStack randomArmorOrWeaponInLevelRange = RunicItemsAPI.generateItemInRange(uncommon.getMinLootLevel(), uncommon.getMaxLootLevel(), 1).generateItem();

        // currency
        ItemStack coin = CurrencyUtil.goldCoin(ThreadLocalRandom.current().nextInt(4, 8 + 1)); // bound is not inclusive, so we add 1

        // food
        ItemStack bread = runicItem("Bread", 2, 4);

        // materials
        ItemStack spruceWood = runicItem("SpruceWood", 3, 5);
        ItemStack oakWood = runicItem("OakWood", 3, 5);
        ItemStack thread = runicItem("Thread", 3, 5);
        ItemStack animalHide = runicItem("AnimalHide", 3, 5);
        ItemStack uncutRuby = runicItem("uncut-ruby", 2, 3);
        ItemStack uncutSapphire = runicItem("uncut-sapphire", 2, 3);
        ItemStack bottle = runicItem("Bottle", 3, 5);
        ItemStack cod = runicItem("Cod", 2, 3);

        // gathering tools (tier 2)
//        ItemStack gatheringAxe = GatheringUtil.GATHERING_AXE_ADEPT_ITEMSTACK;
//        ItemStack gatheringHoe = GatheringUtil.GATHERING_HOE_ADEPT_ITEMSTACK;
//        ItemStack gatheringPick = GatheringUtil.GATHERING_PICKAXE_ADEPT_ITEMSTACK;
//        ItemStack gatheringRod = GatheringUtil.GATHERING_ROD_ADEPT_ITEMSTACK;

        // potions
        ItemStack healthPotion = runicItem("major-potion-healing", 1, 1);
        ItemStack manaPotion = runicItem("major-potion-mana", 1, 1);

        // add entries to table
        uncommonLootTable.addEntry(randomArmorOrWeaponInLevelRange, 50.0);

        uncommonLootTable.addEntry(coin, 50.0);
        uncommonLootTable.addEntry(bread, 35.0);

        uncommonLootTable.addEntry(spruceWood, 8.0);
        uncommonLootTable.addEntry(oakWood, 8.0);
        uncommonLootTable.addEntry(thread, 8.0);
        uncommonLootTable.addEntry(animalHide, 8.0);
        uncommonLootTable.addEntry(uncutRuby, 8.0);
        uncommonLootTable.addEntry(uncutSapphire, 8.0);
        uncommonLootTable.addEntry(bottle, 16.0);
        uncommonLootTable.addEntry(cod, 8.0);

//        uncommonLootTable.addEntry(gatheringAxe, 3.0);
//        uncommonLootTable.addEntry(gatheringHoe, 3.0);
//        uncommonLootTable.addEntry(gatheringPick, 3.0);
//        uncommonLootTable.addEntry(gatheringRod, 5.0);

        uncommonLootTable.addEntry(healthPotion, 25.0);
        uncommonLootTable.addEntry(manaPotion, 25.0);

        return uncommonLootTable;
    }

    public static WeightedRandomBag<ItemStack> lootTableTierIII() {

        // create a loot table object
        WeightedRandomBag<ItemStack> rareLootTable = new WeightedRandomBag<>();

        // armor and weapons
        LootChestTier rare = LootChestTier.TIER_III;
        ItemStack randomArmorOrWeaponInLevelRange = RunicItemsAPI.generateItemInRange(rare.getMinLootLevel(), rare.getMaxLootLevel(), 1).generateItem();

        // currency
        ItemStack coin = CurrencyUtil.goldCoin(ThreadLocalRandom.current().nextInt(5, 10 + 1)); // bound is not inclusive, so we add 1

        // food
        ItemStack bread = runicItem("Bread", 2, 4);

        // crafting materials
        ItemStack spruceWood = runicItem("SpruceWood", 3, 5);
        ItemStack oakWood = runicItem("OakWood", 3, 5);
        ItemStack thread = runicItem("Thread", 3, 5);
        ItemStack animalHide = runicItem("AnimalHide", 3, 5);
        ItemStack uncutRuby = runicItem("uncut-ruby", 2, 3);
        ItemStack uncutSapphire = runicItem("uncut-sapphire", 2, 3);
        ItemStack uncutOpal = runicItem("uncut-opal", 2, 3);
        ItemStack bottle = runicItem("Bottle", 3, 5);
        ItemStack tropical = runicItem("Tropical", 2, 3);

        // gathering tools (tier 3)
//        ItemStack gatheringAxe = GatheringUtil.GATHERING_AXE_REFINED_ITEMSTACK;
//        ItemStack gatheringHoe = GatheringUtil.GATHERING_HOE_REFINED_ITEMSTACK;
//        ItemStack gatheringPick = GatheringUtil.GATHERING_PICKAXE_REFINED_ITEMSTACK;
//        ItemStack gatheringRod = GatheringUtil.GATHERING_ROD_REFINED_ITEMSTACK;

        // potions
        ItemStack healthPotion = runicItem("major-potion-healing", 1, 1);
        ItemStack manaPotion = runicItem("major-potion-mana", 1, 1);

        // add entries to table
        rareLootTable.addEntry(randomArmorOrWeaponInLevelRange, 50.0);
        rareLootTable.addEntry(coin, 50.0);
        rareLootTable.addEntry(bread, 35.0);

        rareLootTable.addEntry(spruceWood, 8.0);
        rareLootTable.addEntry(oakWood, 8.0);
        rareLootTable.addEntry(thread, 8.0);
        rareLootTable.addEntry(animalHide, 8.0);
        rareLootTable.addEntry(uncutRuby, 8.0);
        rareLootTable.addEntry(uncutSapphire, 8.0);
        rareLootTable.addEntry(uncutOpal, 8.0);
        rareLootTable.addEntry(bottle, 16.0);
        rareLootTable.addEntry(tropical, 8.0);

//        rareLootTable.addEntry(gatheringAxe, 2.0);
//        rareLootTable.addEntry(gatheringHoe, 2.0);
//        rareLootTable.addEntry(gatheringPick, 2.0);
//        rareLootTable.addEntry(gatheringRod, 4.0);

        rareLootTable.addEntry(healthPotion, 30.0);
        rareLootTable.addEntry(manaPotion, 30.0);

        return rareLootTable;
    }

    public static WeightedRandomBag<ItemStack> lootTableTierIV() {

        // create a loot table object
        WeightedRandomBag<ItemStack> epicLootTable = new WeightedRandomBag<>();

        // armor and weapons
        LootChestTier epic = LootChestTier.TIER_IV;
        ItemStack randomArmorOrWeaponInLevelRange = RunicItemsAPI.generateItemInRange(epic.getMinLootLevel(), epic.getMaxLootLevel(), 1).generateItem();

        // currency
        ItemStack coin = CurrencyUtil.goldCoin(ThreadLocalRandom.current().nextInt(5, 10 + 1)); // bound is not inclusive, so we add 1

        // food
        ItemStack bread = runicItem("Bread", 2, 4);

        // materials
        ItemStack spruceWood = runicItem("SpruceWood", 3, 5);
        ItemStack oakWood = runicItem("OakWood", 3, 5);
        ItemStack thread = runicItem("Thread", 3, 5);
        ItemStack animalHide = runicItem("AnimalHide", 3, 5);
        ItemStack uncutRuby = runicItem("uncut-ruby", 2, 3);
        ItemStack uncutSapphire = runicItem("uncut-sapphire", 2, 3);
        ItemStack uncutOpal = runicItem("uncut-opal", 2, 3);
        ItemStack uncutEmerald = runicItem("uncut-emerald", 2, 3);
        ItemStack uncutDiamond = runicItem("uncut-diamond", 2, 3);
        ItemStack bottle = runicItem("Bottle", 3, 5);
        ItemStack pufferfish = runicItem("Pufferfish", 2, 3);

        // gathering tools (tier 4)
//        ItemStack gatheringAxe = GatheringUtil.GATHERING_AXE_MASTER_ITEMSTACK;
//        ItemStack gatheringHoe = GatheringUtil.GATHERING_HOE_MASTER_ITEMSTACK;
//        ItemStack gatheringPick = GatheringUtil.GATHERING_PICKAXE_MASTER_ITEMSTACK;
//        ItemStack gatheringRod = GatheringUtil.GATHERING_ROD_MASTER_ITEMSTACK;

        // potions
        ItemStack healthPotion = runicItem("greater-potion-healing", 1, 1);
        ItemStack manaPotion = runicItem("greater-potion-mana", 1, 1);

        // add entries to table
        epicLootTable.addEntry(randomArmorOrWeaponInLevelRange, 30.0);
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

        epicLootTable.addEntry(bottle, 16.0);
        epicLootTable.addEntry(pufferfish, 8.0);

//        epicLootTable.addEntry(gatheringAxe, 2.0);
//        epicLootTable.addEntry(gatheringHoe, 2.0);
//        epicLootTable.addEntry(gatheringPick, 2.0);
//        epicLootTable.addEntry(gatheringRod, 4.0);

        epicLootTable.addEntry(healthPotion, 30.0);
        epicLootTable.addEntry(manaPotion, 30.0);

        return epicLootTable;
    }

    /**
     * Creates an item stack which is a current runic item
     *
     * @param templateId   the id of the runic item
     * @param minStackSize min stack size of item
     * @param maxStackSize max stack size of item
     * @return item stack
     */
    private static ItemStack runicItem(String templateId, int minStackSize, int maxStackSize) {
        return RunicItemsAPI.generateItemFromTemplate(templateId, (ThreadLocalRandom.current().nextInt(minStackSize, maxStackSize + 1))).generateItem();
    }

    @Override
    public WeightedRandomBag<ItemStack> getLootTableTierI() {
        return LOOT_TABLE_TIER_I;
    }
}
