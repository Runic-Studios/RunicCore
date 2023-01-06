package com.runicrealms.plugin.item.lootchests;

public enum BossChestTier {

    SEBATHS_CAVE(0, 1, 9, 2, 4),
    CRYSTAL_CAVERN(10, 10, 24, 2, 4),
    JORUNDRS_KEEP(25, 25, 39, 3, 5),
    SUNKEN_LIBRARY(40, 40, 60, 3, 6);

    private final int minAccessLevel;
    private final int minLootLevel;
    private final int maxLootLevel;
    private final int minimumItems;
    private final int maximumItems;

    /**
     * Enumerates a set of loot chest rarities which determine the content of the box
     *
     * @param minAccessLevel the minimum level to open the box
     * @param minLootLevel   the minimum level of the item loot
     * @param maxLootLevel   the max level of the item loot
     * @param minimumItems   the min number of items in the box
     * @param maximumItems   the max number of items in the box
     */
    BossChestTier(int minAccessLevel, int minLootLevel, int maxLootLevel, int minimumItems, int maximumItems) {
        this.minAccessLevel = minAccessLevel;
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

    public int getMinAccessLevel() {
        return minAccessLevel;
    }

    public int getMinLootLevel() {
        return minLootLevel;
    }

    public int getMinimumItems() {
        return minimumItems;
    }

}
