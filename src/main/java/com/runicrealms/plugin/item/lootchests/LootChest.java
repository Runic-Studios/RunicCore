package com.runicrealms.plugin.item.lootchests;

import org.bukkit.Location;

public class LootChest {

    private String tier;
    private Location location;

    public LootChest(String tier, Location location) {
        this.tier = tier;
        this.location = location;
    }

    public String getTier() {
        return tier;
    }

    public Location getLocation() {
        return location;
    }
}
