package com.runicrealms.plugin.api;

import com.runicrealms.plugin.item.lootchests.WeightedRandomBag;
import org.bukkit.inventory.ItemStack;

public interface LootTableAPI {

    /**
     * @return
     */
    WeightedRandomBag<ItemStack> getLootTableTierI();
}
