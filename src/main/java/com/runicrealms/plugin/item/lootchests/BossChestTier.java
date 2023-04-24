package com.runicrealms.plugin.item.lootchests;

public enum BossChestTier {

    SEBATHS_CAVE(3, 9, 3, 5),
    CRYSTAL_CAVERN(10, 18, 3, 5),
    JORUNDRS_KEEP(15, 24, 3, 6),
    SUNKEN_LIBRARY(25, 34, 3, 6),
    CRYPTS_OF_DERA(35, 50, 3, 7),
    FROZEN_FORTRESS(53, 60, 4, 8);

    private final int minLootLevel;
    private final int maxLootLevel;
    private final int minimumItems;
    private final int maximumItems;

    /**
     * Enumerates a set of loot chest rarities which determine the content of the box
     *
     * @param minLootLevel the minimum level of the item loot
     * @param maxLootLevel the max level of the item loot
     * @param minimumItems the min number of items in the box
     * @param maximumItems the max number of items in the box
     */
    BossChestTier(int minLootLevel, int maxLootLevel, int minimumItems, int maximumItems) {
        this.minLootLevel = minLootLevel;
        this.maxLootLevel = maxLootLevel;
        this.minimumItems = minimumItems;
        this.maximumItems = maximumItems;
    }

    /**
     * Returns an enum based on a string identifier
     *
     * @param identifier the name of the rarity
     * @return an enum
     */
    public static LootChestTier getFromIdentifier(String identifier) {
        for (LootChestTier lootChestTier : LootChestTier.values()) {
            if (lootChestTier.getIdentifier().equals(identifier))
                return lootChestTier;
        }
        return null;
    }

    public int getMaxLootLevel() {
        return maxLootLevel;
    }

    public int getMaximumItems() {
        return maximumItems;
    }

    public int getMinLootLevel() {
        return minLootLevel;
    }

    public int getMinimumItems() {
        return minimumItems;
    }

}
