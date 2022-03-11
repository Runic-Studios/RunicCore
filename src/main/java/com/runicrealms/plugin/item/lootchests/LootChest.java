package com.runicrealms.plugin.item.lootchests;

import org.bukkit.Location;

public class LootChest {

    private final String id;
    private final LootChestTier lootChestTier;
    private final Location location;

    /**
     * Create a loot chest for in-memory storage
     *
     * @param id of the loot chest in yaml file
     * @param lootChestTier rarity of loot chest
     * @param location of the chest
     */
    public LootChest(String id, LootChestTier lootChestTier, Location location) {
        this.id = id;
        this.lootChestTier = lootChestTier;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public LootChestTier getLootChestRarity() {
        return lootChestTier;
    }

    public Location getLocation() {
        return location;
    }
}
