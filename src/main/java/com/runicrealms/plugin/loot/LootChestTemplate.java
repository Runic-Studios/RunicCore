package com.runicrealms.plugin.loot;

import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class LootChestTemplate {

    private final String identifier;
    private final LootTable lootTable;
    private final int minCount;
    private final int maxCount;
    private final int inventorySize;

    public LootChestTemplate(String identifier, LootTable lootTable, int minCount, int maxCount, int inventorySize) {
        this.identifier = identifier;
        this.lootTable = lootTable;
        if (minCount > maxCount)
            throw new IllegalArgumentException("LootChestTemplate min count cannot exceed max count!");
        this.minCount = minCount;
        this.maxCount = maxCount;
        if (inventorySize % 9 != 0 || inventorySize < maxCount)
            throw new IllegalArgumentException("Cannot create LootChestTemplate " + identifier + " with inventory size " + inventorySize);
        this.inventorySize = inventorySize;
    }

    public LootChestInventory generateInventory(LootChest lootChest) {
        Random rand = new Random();
        int itemCount = rand.nextInt(maxCount - minCount + 1) + minCount;
        Set<ItemStack> items = new HashSet<>();
        for (int i = 0; i < itemCount; i++) items.add(lootTable.generateLoot(lootChest));
        return new LootChestInventory(items, inventorySize, lootChest.getInventoryTitle(), null);
    }

    public String getIdentifier() {
        return this.identifier;
    }

}
