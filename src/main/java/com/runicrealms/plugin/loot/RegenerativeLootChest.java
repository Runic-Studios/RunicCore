package com.runicrealms.plugin.loot;

public class RegenerativeLootChest extends LootChest {

    private final int regenerationTime;

    public RegenerativeLootChest(
            LootChestLocation location,
            LootChestTemplate lootChestTemplate,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            int regenerationTime,
            String inventoryTitle) {
        super(location, lootChestTemplate, minLevel, itemMinLevel, itemMaxLevel, inventoryTitle);
        this.regenerationTime = regenerationTime;
    }

    public int getRegenerationTime() {
        return this.regenerationTime;
    }
}
