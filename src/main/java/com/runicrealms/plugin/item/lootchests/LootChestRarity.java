package com.runicrealms.plugin.item.lootchests;

import org.bukkit.Color;

public enum LootChestRarity {

    COMMON("common", Color.WHITE, 3, 1, 9, 600), // 10 min
    UNCOMMON("uncommon", Color.LIME, 10, 10, 24, 900), // 15 min
    RARE("rare", Color.AQUA, 25, 25, 39, 1200), // 20 min
    EPIC("epic", Color.FUCHSIA, 40, 40, 60, 2700); // 45 min

    private final String identifier;
    private final Color color;
    private final int minAccessLevel;
    private final int minLootLevel;
    private final int maxLootLevel;
    private final int respawnTime;

    LootChestRarity(String identifier, Color color, int minAccessLevel, int minLootLevel, int maxLootLevel, int respawnTime) {
        this.identifier = identifier;
        this.color = color;
        this.minAccessLevel = minAccessLevel;
        this.minLootLevel = minLootLevel;
        this.maxLootLevel = maxLootLevel;
        this.respawnTime = respawnTime;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Color getColor() {
        return color;
    }

    public int getMinAccessLevel() {
        return minAccessLevel;
    }

    public int getMinLootLevel() {
        return minLootLevel;
    }

    public int getMaxLootLevel() {
        return maxLootLevel;
    }

    public int getRespawnTime() {
        return respawnTime;
    }

    /**
     * Returns an enum based on a string identifier
     *
     * @param identifier the name of the rarity
     * @return an enum
     */
    public static LootChestRarity getFromIdentifier(String identifier) {
        for (LootChestRarity lootChestRarity : LootChestRarity.values()) {
            if (lootChestRarity.getIdentifier().equals(identifier))
                return lootChestRarity;
        }
        return null;
    }
}
