package com.runicrealms.plugin.loot;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class LootChestPosition {

    private final Location location;
    private final BlockFace direction;

    public LootChestPosition(Location location, BlockFace direction) {
        this.location = location;
        this.direction = direction;
    }

    public Location getLocation() {
        return this.location;
    }

    public BlockFace getDirection() {
        return this.direction;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LootChestPosition lootChestPosition) && lootChestPosition.getLocation().equals(this.location);
    }
}
