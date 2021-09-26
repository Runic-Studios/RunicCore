package com.runicrealms.plugin.item.lootchests;

import org.bukkit.Location;

public class LootChest {

    private final String id;
    private final LootChestRarity lootChestRarity;
    private final Location location;

    /**
     * Create a loot chest for in-memory storage
     *
     * @param id of the loot chest in yaml file
     * @param lootChestRarity rarity of loot chest
     * @param location of the chest
     */
    public LootChest(String id, LootChestRarity lootChestRarity, Location location) {
        this.id = id;
        this.lootChestRarity = lootChestRarity;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public LootChestRarity getLootChestRarity() {
        return lootChestRarity;
    }

    public Location getLocation() {
        return location;
    }
}
