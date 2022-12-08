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
    private static WeightedRandomBag<ItemStack> LOOT_TABLE_TIER_II;
    private static WeightedRandomBag<ItemStack> LOOT_TABLE_TIER_III;
    private static WeightedRandomBag<ItemStack> LOOT_TABLE_TIER_IV;


    static {
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            setupCommonLootTable();
            setupUncommonLootTable();
            setupRareLootTable();
            setupEpicLootTable();
        }, 10 * 20L);
    }

    private static void setupCommonLootTable() {
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
    }

    private static void setupUncommonLootTable() {
        // create a loot table object
        LOOT_TABLE_TIER_II = new WeightedRandomBag<>();

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

        // potions
        ItemStack healthPotion = runicItem("major-potion-healing", 1, 1);
        ItemStack manaPotion = runicItem("major-potion-mana", 1, 1);

        // add entries to table
        LOOT_TABLE_TIER_II.addEntry(randomArmorOrWeaponInLevelRange, 50.0);

        LOOT_TABLE_TIER_II.addEntry(coin, 50.0);
        LOOT_TABLE_TIER_II.addEntry(bread, 35.0);

        LOOT_TABLE_TIER_II.addEntry(spruceWood, 8.0);
        LOOT_TABLE_TIER_II.addEntry(oakWood, 8.0);
        LOOT_TABLE_TIER_II.addEntry(thread, 8.0);
        LOOT_TABLE_TIER_II.addEntry(animalHide, 8.0);
        LOOT_TABLE_TIER_II.addEntry(uncutRuby, 8.0);
        LOOT_TABLE_TIER_II.addEntry(uncutSapphire, 8.0);
        LOOT_TABLE_TIER_II.addEntry(bottle, 16.0);
        LOOT_TABLE_TIER_II.addEntry(cod, 8.0);

        LOOT_TABLE_TIER_II.addEntry(healthPotion, 25.0);
        LOOT_TABLE_TIER_II.addEntry(manaPotion, 25.0);
    }

    private static void setupRareLootTable() {
        // create a loot table object
        LOOT_TABLE_TIER_III = new WeightedRandomBag<>();

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

        // potions
        ItemStack healthPotion = runicItem("major-potion-healing", 1, 1);
        ItemStack manaPotion = runicItem("major-potion-mana", 1, 1);

        // add entries to table
        LOOT_TABLE_TIER_III.addEntry(randomArmorOrWeaponInLevelRange, 50.0);
        LOOT_TABLE_TIER_III.addEntry(coin, 50.0);
        LOOT_TABLE_TIER_III.addEntry(bread, 35.0);

        LOOT_TABLE_TIER_III.addEntry(spruceWood, 8.0);
        LOOT_TABLE_TIER_III.addEntry(oakWood, 8.0);
        LOOT_TABLE_TIER_III.addEntry(thread, 8.0);
        LOOT_TABLE_TIER_III.addEntry(animalHide, 8.0);
        LOOT_TABLE_TIER_III.addEntry(uncutRuby, 8.0);
        LOOT_TABLE_TIER_III.addEntry(uncutSapphire, 8.0);
        LOOT_TABLE_TIER_III.addEntry(uncutOpal, 8.0);
        LOOT_TABLE_TIER_III.addEntry(bottle, 16.0);
        LOOT_TABLE_TIER_III.addEntry(tropical, 8.0);

        LOOT_TABLE_TIER_III.addEntry(healthPotion, 30.0);
        LOOT_TABLE_TIER_III.addEntry(manaPotion, 30.0);
    }

    private static void setupEpicLootTable() {
        // create a loot table object
        LOOT_TABLE_TIER_IV = new WeightedRandomBag<>();

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

        // potions
        ItemStack healthPotion = runicItem("greater-potion-healing", 1, 1);
        ItemStack manaPotion = runicItem("greater-potion-mana", 1, 1);

        // add entries to table
        LOOT_TABLE_TIER_IV.addEntry(randomArmorOrWeaponInLevelRange, 30.0);
        LOOT_TABLE_TIER_IV.addEntry(coin, 50.0);
        LOOT_TABLE_TIER_IV.addEntry(bread, 35.0);

        LOOT_TABLE_TIER_IV.addEntry(spruceWood, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(oakWood, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(thread, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(animalHide, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutRuby, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutSapphire, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutOpal, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutEmerald, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutDiamond, 8.0);

        LOOT_TABLE_TIER_IV.addEntry(bottle, 16.0);
        LOOT_TABLE_TIER_IV.addEntry(pufferfish, 8.0);

        LOOT_TABLE_TIER_IV.addEntry(healthPotion, 30.0);
        LOOT_TABLE_TIER_IV.addEntry(manaPotion, 30.0);
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
    public WeightedRandomBag<ItemStack> getLootTableIV() {
        return LOOT_TABLE_TIER_IV;
    }

    @Override
    public WeightedRandomBag<ItemStack> getLootTableTierI() {
        return LOOT_TABLE_TIER_I;
    }

    @Override
    public WeightedRandomBag<ItemStack> getLootTableTierII() {
        return LOOT_TABLE_TIER_II;
    }

    @Override
    public WeightedRandomBag<ItemStack> getLootTableTierIII() {
        return LOOT_TABLE_TIER_III;
    }
}
