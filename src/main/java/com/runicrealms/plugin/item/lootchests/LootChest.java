package com.runicrealms.plugin.item.lootchests;

import org.bukkit.Location;

public class LootChest {

    private final String id;
    private final String tier;
    private final Location location;

    /**
     *
     * @param id
     * @param tier
     * @param location
     */
    public LootChest(String id, String tier, Location location) {
        this.id = id;
        this.tier = tier;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public String getTier() {
        return tier;
    }

    public Location getLocation() {
        return location;
    }
}
