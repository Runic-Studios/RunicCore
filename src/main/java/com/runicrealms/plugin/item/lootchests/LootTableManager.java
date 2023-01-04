package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.LootTableAPI;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A util to retrieve weighted loot tables for each tier of loot chest.
 */
public class LootTableManager implements LootTableAPI {

    private static WeightedRandomBag<ChestItem> LOOT_TABLE_TIER_I;
    private static WeightedRandomBag<ChestItem> LOOT_TABLE_TIER_II;
    private static WeightedRandomBag<ChestItem> LOOT_TABLE_TIER_III;
    private static WeightedRandomBag<ChestItem> LOOT_TABLE_TIER_IV;


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
        ChestItem randomArmorOrWeaponInLevelRange = new ChestItem();

        ChestItem coin = new ChestItem("Coin", 3, 6);
        ChestItem bread = new ChestItem("Bread", 2, 4);

        // materials
        ChestItem spruceWood = new ChestItem("SpruceWood", 3, 5);
        ChestItem oakWood = new ChestItem("OakWood", 3, 5);
        ChestItem thread = new ChestItem("Thread", 3, 5);
        ChestItem animalHide = new ChestItem("AnimalHide", 3, 5);
        ChestItem uncutRuby = new ChestItem("uncut-ruby", 2, 3);
        ChestItem bottle = new ChestItem("Bottle", 2, 3);
        ChestItem salmon = new ChestItem("Salmon", 2, 3);

        // potions
        ChestItem healthPotion = new ChestItem("minor-potion-healing", 1, 1);
        ChestItem manaPotion = new ChestItem("minor-potion-mana", 1, 1);

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
        ChestItem randomArmorOrWeaponInLevelRange = new ChestItem();

        // currency
        ChestItem coin = new ChestItem("Coin", 4, 8);

        // food
        ChestItem bread = new ChestItem("Bread", 2, 4);

        // materials
        ChestItem spruceWood = new ChestItem("SpruceWood", 3, 5);
        ChestItem oakWood = new ChestItem("OakWood", 3, 5);
        ChestItem thread = new ChestItem("Thread", 3, 5);
        ChestItem animalHide = new ChestItem("AnimalHide", 3, 5);
        ChestItem uncutRuby = new ChestItem("uncut-ruby", 2, 3);
        ChestItem uncutSapphire = new ChestItem("uncut-sapphire", 2, 3);
        ChestItem bottle = new ChestItem("Bottle", 3, 5);
        ChestItem cod = new ChestItem("Cod", 2, 3);

        // potions
        ChestItem healthPotion = new ChestItem("major-potion-healing", 1, 1);
        ChestItem manaPotion = new ChestItem("major-potion-mana", 1, 1);

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
        ChestItem randomArmorOrWeaponInLevelRange = new ChestItem();

        // currency
        ChestItem coin = new ChestItem("Coin", 5, 10);

        // food
        ChestItem bread = new ChestItem("Bread", 2, 4);

        // crafting materials
        ChestItem spruceWood = new ChestItem("SpruceWood", 3, 5);
        ChestItem oakWood = new ChestItem("OakWood", 3, 5);
        ChestItem thread = new ChestItem("Thread", 3, 5);
        ChestItem animalHide = new ChestItem("AnimalHide", 3, 5);
        ChestItem uncutRuby = new ChestItem("uncut-ruby", 2, 3);
        ChestItem uncutSapphire = new ChestItem("uncut-sapphire", 2, 3);
        ChestItem uncutOpal = new ChestItem("uncut-opal", 2, 3);
        ChestItem bottle = new ChestItem("Bottle", 3, 5);
        ChestItem tropical = new ChestItem("Tropical", 2, 3);

        // potions
        ChestItem healthPotion = new ChestItem("major-potion-healing", 1, 1);
        ChestItem manaPotion = new ChestItem("major-potion-mana", 1, 1);

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
        ChestItem randomArmorOrWeaponInLevelRange = new ChestItem();

        // currency
        ChestItem coin = new ChestItem("Coin", 6, 12);

        // food
        ChestItem bread = new ChestItem("Bread", 2, 4);

        // materials
        ChestItem spruceWood = new ChestItem("SpruceWood", 3, 5);
        ChestItem oakWood = new ChestItem("OakWood", 3, 5);
        ChestItem thread = new ChestItem("Thread", 3, 5);
        ChestItem animalHide = new ChestItem("AnimalHide", 3, 5);
        ChestItem uncutRuby = new ChestItem("uncut-ruby", 2, 3);
        ChestItem uncutSapphire = new ChestItem("uncut-sapphire", 2, 3);
        ChestItem uncutOpal = new ChestItem("uncut-opal", 2, 3);
        ChestItem uncutEmerald = new ChestItem("uncut-emerald", 2, 3);
        ChestItem uncutDiamond = new ChestItem("uncut-diamond", 2, 3);
        ChestItem bottle = new ChestItem("Bottle", 3, 5);
        ChestItem pufferfish = new ChestItem("Pufferfish", 2, 3);

        // potions
        ChestItem healthPotion = new ChestItem("greater-potion-healing", 1, 1);
        ChestItem manaPotion = new ChestItem("greater-potion-mana", 1, 1);

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

    @Override
    public ItemStack generateItemStack(ChestItem chestItem, LootChestTier lootChestTier) {
        if (chestItem.isScriptItem())
            return RunicItemsAPI.generateItemInRange(lootChestTier.getMinLootLevel(), lootChestTier.getMaxLootLevel(), 1).generateItem();
        String templateID = chestItem.getTemplateID();
        int minStackSize = chestItem.getMin();
        int maxStackSize = chestItem.getMax();
        // Bound is not inclusive, so we add 1
        return RunicItemsAPI.generateItemFromTemplate(templateID, (ThreadLocalRandom.current().nextInt(minStackSize, maxStackSize + 1))).generateItem();
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableTierI() {
        return LOOT_TABLE_TIER_I;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableTierII() {
        return LOOT_TABLE_TIER_II;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableTierIII() {
        return LOOT_TABLE_TIER_III;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableTierIV() {
        return LOOT_TABLE_TIER_IV;
    }
}
