package com.runicrealms.plugin.loot;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class LootChestLocation {

    private final Location bukkitLocation;
    private final BlockFace direction;

    public LootChestLocation(Location bukkitLocation, BlockFace direction) {
        this.bukkitLocation = bukkitLocation;
        this.direction = direction;
    }

    public Location getBukkitLocation() {
        return this.bukkitLocation;
    }

    public BlockFace getDirection() {
        return this.direction;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LootChestLocation lootChestLocation) && lootChestLocation.getBukkitLocation().equals(this.bukkitLocation);
    }
}
