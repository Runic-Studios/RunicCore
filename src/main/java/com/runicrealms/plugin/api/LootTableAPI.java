package com.runicrealms.plugin.api;

import com.runicrealms.plugin.item.lootchests.WeightedRandomBag;
import org.bukkit.inventory.ItemStack;

public interface LootTableAPI {

    /**
     * @return the loot/drop table for the common tier
     */
    WeightedRandomBag<ItemStack> getLootTableTierI();

    /**
     * @return the loot/drop table for the uncommon tier
     */
    WeightedRandomBag<ItemStack> getLootTableTierII();

    /**
     * @return the loot/drop table for the rare tier
     */
    WeightedRandomBag<ItemStack> getLootTableTierIII();

    /**
     * @return the loot/drop table for the epic tier
     */
    WeightedRandomBag<ItemStack> getLootTableTierIV();
}
