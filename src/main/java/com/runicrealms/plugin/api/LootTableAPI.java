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
     * @return the loot/drop table for sebath's cave
     */
    WeightedRandomBag<ChestItem> getLootTableSebaths();

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
