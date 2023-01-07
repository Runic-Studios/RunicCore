package com.runicrealms.plugin.api;

import com.runicrealms.plugin.item.lootchests.BossChestTier;
import com.runicrealms.plugin.item.lootchests.ChestItem;
import com.runicrealms.plugin.item.lootchests.LootChestTier;
import com.runicrealms.plugin.item.lootchests.WeightedRandomBag;
import org.bukkit.inventory.ItemStack;

public interface LootTableAPI {

    /**
     * Generates a runic item ItemStack from the chest item wrapper
     *
     * @param chestItem     the info to create the ItemStack (templateID, stack size)
     * @param bossChestTier tier of the boss chest (Sebath's cave, Sunken Library, etc.)
     */
    ItemStack generateItemStack(ChestItem chestItem, BossChestTier bossChestTier);

    /**
     * Generates a runic item ItemStack from the chest item wrapper
     *
     * @param chestItem     the info to create the ItemStack (templateID, stack size)
     * @param lootChestTier tier of the loot chest (common, rare, etc.)
     */
    ItemStack generateItemStack(ChestItem chestItem, LootChestTier lootChestTier);

    /**
     * @return the loot/drop table for Crypts of Dera
     */
    WeightedRandomBag<ChestItem> getLootTableCryptsOfDera();

    /**
     * @return the loot/drop table for Crystal Cavern
     */
    WeightedRandomBag<ChestItem> getLootTableCrystalCavern();

    /**
     * @return the loot/drop table for Frozen Fortress
     */
    WeightedRandomBag<ChestItem> getLootTableFrozenFortress();

    /**
     * @return the loot/drop table for Jorundr's cave
     */
    WeightedRandomBag<ChestItem> getLootTableJorundrsKeep();

    /**
     * @return the loot/drop table for sebath's cave
     */
    WeightedRandomBag<ChestItem> getLootTableSebathsCave();

    /**
     * @return the loot/drop table for jorundr's cave
     */
    WeightedRandomBag<ChestItem> getLootTableSunkenLibrary();

    /**
     * @return the loot/drop table for the common tier
     */
    WeightedRandomBag<ChestItem> getLootTableTierI();

    /**
     * @return the loot/drop table for the uncommon tier
     */
    WeightedRandomBag<ChestItem> getLootTableTierII();

    /**
     * @return the loot/drop table for the rare tier
     */
    WeightedRandomBag<ChestItem> getLootTableTierIII();

    /**
     * @return the loot/drop table for the epic tier
     */
    WeightedRandomBag<ChestItem> getLootTableTierIV();
}
