package com.runicrealms.plugin.loot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

public abstract class LootChest implements LootHolder {

    protected static final BlockData AIR_BLOCK_DATA = Material.AIR.createBlockData();

    protected final LootChestPosition position;
    protected final LootChestTemplate lootChestTemplate;
    protected final LootChestConditions conditions;
    protected final int minLevel;
    protected final int itemMinLevel;
    protected final int itemMaxLevel;
    protected final String inventoryTitle;

    protected final BlockData blockData;
    protected final PacketContainer chestOpenPacket;
    protected final PacketContainer chestClosePacket;

    public LootChest(
            LootChestPosition position,
            LootChestTemplate lootChestTemplate,
            LootChestConditions conditions,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            String inventoryTitle) {
        this.position = position;
        this.lootChestTemplate = lootChestTemplate;
        this.conditions = conditions;
        this.minLevel = minLevel;
        this.itemMinLevel = itemMinLevel;
        this.itemMaxLevel = itemMaxLevel;
        this.inventoryTitle = inventoryTitle;

        this.blockData = Material.CHEST.createBlockData();
        ((Directional) this.blockData).setFacing(this.position.getDirection());

        BlockPosition packetPosition = new BlockPosition(position.getLocation().getBlockX(), position.getLocation().getBlockY(), position.getLocation().getBlockZ());

        chestOpenPacket = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);
        chestOpenPacket.getBlockPositionModifier().write(0, packetPosition);
        chestOpenPacket.getIntegers().write(0, 1); // Action ID for chest (1)
        chestOpenPacket.getIntegers().write(1, 1); // Number of players viewing chest (anything above 0 -> open)
        chestOpenPacket.getBlocks().write(0, Material.CHEST); // Block type

        chestClosePacket = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);
        chestClosePacket.getBlockPositionModifier().write(0, packetPosition);
        chestClosePacket.getIntegers().write(0, 1); // Action ID for chest (1)
        chestClosePacket.getIntegers().write(1, 0); // Number of players viewing chest (anything above 0 -> open)
        chestClosePacket.getBlocks().write(0, Material.CHEST); // Block type
    }

    public LootChestPosition getPosition() {
        return this.position;
    }

    public LootChestTemplate getLootChestTemplate() {
        return this.lootChestTemplate;
    }

    public LootChestConditions getConditions() {
        return this.conditions;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    @Override
    public int getItemMinLevel(Player player) {
        return getItemMinLevel();
    }

    public int getItemMinLevel() {
        return this.itemMinLevel;
    }

    @Override
    public int getItemMaxLevel(Player player) {
        return getItemMaxLevel();
    }

    public int getItemMaxLevel() {
        return this.itemMaxLevel;
    }

    public String getInventoryTitle() {
        return this.inventoryTitle;
    }

    protected LootChestInventory generateInventory(Player player) {
        return lootChestTemplate.generateInventory(this, player);
    }

    public void openInventory(Player player) {
        boolean canOpen = this.conditions.attempt(player);
        if (canOpen) generateInventory(player).open(player);
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

    public void packetOpenForPlayer(Player player) {
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, chestOpenPacket);
    }

    public void packetCloseForPlayer(Player player) {
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, chestClosePacket);
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
