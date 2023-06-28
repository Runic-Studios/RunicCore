package com.runicrealms.plugin.loot;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

public abstract class LootChest {

    protected static final BlockData AIR_BLOCK_DATA = Material.AIR.createBlockData();

    protected final LootChestPosition position;
    protected final LootChestTemplate lootChestTemplate;
    protected final int minLevel;
    protected final int itemMinLevel;
    protected final int itemMaxLevel;
    protected final String inventoryTitle;

    protected final BlockData blockData;

    public LootChest(LootChestPosition position, LootChestTemplate lootChestTemplate, int minLevel, int itemMinLevel, int itemMaxLevel, String inventoryTitle) {
        this.inventoryTitle = inventoryTitle;
        this.position = position;
        this.minLevel = minLevel;
        this.itemMinLevel = itemMinLevel;
        this.itemMaxLevel = itemMaxLevel;
        this.lootChestTemplate = lootChestTemplate;
        this.blockData = Material.CHEST.createBlockData();
        ((Directional) this.blockData).setFacing(this.position.getDirection());

    }

    public String getInventoryTitle() {
        return this.inventoryTitle;
    }

    public LootChestPosition getPosition() {
        return this.position;
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

    protected LootChestInventory generateInventory() {
        return lootChestTemplate.generateInventory(this);
    }

    public void openInventory(Player player) {
        generateInventory().open(player);
    }

    public void showToPlayer(Player player) {
        if (this.position.getLocation().getBlock().getType() != Material.AIR) {
            this.position.getLocation().getBlock().setType(Material.AIR);
        }
        player.sendBlockChange(position.getLocation(), blockData);
    }

    public void hideFromPlayer(Player player) {
        player.sendBlockChange(position.getLocation(), AIR_BLOCK_DATA);
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof LootChest lootChest) && lootChest.position.equals(position);
    }

    /**
     * Declares if this loot chest should have its block updated automatically by the ClientLootManager.
     */
    public abstract boolean shouldUpdateDisplay();

}
