package com.runicrealms.plugin.loot;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

public class LootChest {

    protected final LootChestLocation location;
    protected final LootChestTemplate lootChestTemplate;
    protected final int minLevel;
    protected final int itemMinLevel;
    protected final int itemMaxLevel;
    protected final String inventoryTitle;

    protected final BlockData blockData;

    public LootChest(LootChestLocation location, LootChestTemplate lootChestTemplate, int minLevel, int itemMinLevel, int itemMaxLevel, String inventoryTitle) {
        this.inventoryTitle = inventoryTitle;
        this.location = location;
        this.minLevel = minLevel;
        this.itemMinLevel = itemMinLevel;
        this.itemMaxLevel = itemMaxLevel;
        this.lootChestTemplate = lootChestTemplate;
        this.blockData = Material.CHEST.createBlockData();
        ((Directional) this.blockData).setFacing(this.location.getDirection());
    }

    public String getInventoryTitle() {
        return this.inventoryTitle;
    }

    public LootChestLocation getLocation() {
        return this.location;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getItemMinLevel() {
        return this.itemMinLevel;
    }

    public int getItemMaxLevel() {
        return this.itemMaxLevel;
    }

    public LootChestTemplate getLootChestTemplate() {
        return this.lootChestTemplate;
    }

    public void openInventory(Player player) {
        this.lootChestTemplate.generateInventory(this).open(player);
    }

    public void setChestBlock(Player player) {
        player.sendBlockChange(location.getBukkitLocation(), blockData);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LootChest lootChest)) return false;
        return lootChest.location.equals(location);
    }

}
