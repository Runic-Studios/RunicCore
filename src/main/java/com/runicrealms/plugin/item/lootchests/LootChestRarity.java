package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.enums.DungeonLocation;

public enum LootChestRarity {

    COMMON("common"),
    UNCOMMON("uncommon"),
    RARE("rare"),
    EPIC("epic");

    private final String identifier;

    LootChestRarity(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
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
