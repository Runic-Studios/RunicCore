package com.runicrealms.plugin.loot.chest;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.loot.LootHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LootChest implements LootHolder {

    protected static final BlockData AIR_BLOCK_DATA = Material.AIR.createBlockData();

    protected final LootChestPosition position;
    protected final LootChestTemplate lootChestTemplate;
    protected final LootChestConditions conditions;
    protected final int minLevel;
    protected final int itemMinLevel;
    protected final int itemMaxLevel;
    protected final String inventoryTitle;
    /*
    protected final ModeledEntity entity;
    private final String modelID;
    protected ActiveModel model;
     */

    protected final BlockData blockData;

    public LootChest(
            @NotNull LootChestPosition position,
            @NotNull LootChestTemplate lootChestTemplate,
            @NotNull LootChestConditions conditions,
            int minLevel,
            int itemMinLevel, int itemMaxLevel,
            @NotNull String inventoryTitle,
            @Nullable String modelID) {
        this.position = position;
        this.lootChestTemplate = lootChestTemplate;
        this.conditions = conditions;
        this.minLevel = minLevel;
        this.itemMinLevel = itemMinLevel;
        this.itemMaxLevel = itemMaxLevel;
        this.inventoryTitle = inventoryTitle;
        /*
        this.modelID = modelID;

        Dummy dummy = ModelEngineAPI.createDummy();
        Location target = position.getLocation().clone().setDirection(position.getDirection().getDirection());
        dummy.setLocation(target);

        this.entity = ModelEngineAPI.createModeledEntity(dummy);
        this.entity.getRangeManager().setRenderDistance(0);
         */

        this.blockData = Material.CHEST.createBlockData();
        ((Directional) this.blockData).setFacing(this.position.getDirection());

        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> this.position.getLocation().getBlock().setType(Material.AIR), 10);
    }

    public LootChest(@NotNull LootChestPosition position, @NotNull LootChestTemplate lootChestTemplate, @NotNull LootChestConditions conditions, int minLevel, int itemMinLevel, int itemMaxLevel, @NotNull String inventoryTitle) {
        this(position, lootChestTemplate, conditions, minLevel, itemMinLevel, itemMaxLevel, inventoryTitle, null);
    }

    @NotNull
    public LootChestPosition getPosition() {
        return this.position;
    }

    @NotNull
    public LootChestTemplate getLootChestTemplate() {
        return this.lootChestTemplate;
    }

    @NotNull
    public LootChestConditions getConditions() {
        return this.conditions;
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    @Override
    public int getItemMinLevel(@NotNull Player player) {
        return getItemMinLevel();
    }

    public int getItemMinLevel() {
        return this.itemMinLevel;
    }

    @Override
    public int getItemMaxLevel(@NotNull Player player) {
        return getItemMaxLevel();
    }

    public int getItemMaxLevel() {
        return this.itemMaxLevel;
    }

    public String getInventoryTitle() {
        return this.inventoryTitle;
    }

    protected LootChestInventory generateInventory(@NotNull Player player) {
        return lootChestTemplate.generateInventory(this, player);
    }

    public void openInventory(@NotNull Player player) {
        boolean canOpen = this.conditions.attempt(player);

        if (!canOpen) {
            return;
        }

        this.generateInventory(player).open(player);

        /*
        this.setActiveModel();

        this.model.getAnimationHandler().playAnimation("hit", 1, 1, 1, true);
         */
    }

    public void showToPlayer(@NotNull Player player) {
        player.sendBlockChange(this.position.getLocation(), this.blockData);
        /*
        this.setActiveModel();

        if (this.model == null) {
            return;
        }

        this.model.showToPlayer(player);
         */
    }

    public void hideFromPlayer(@NotNull Player player) {
        player.sendBlockChange(this.position.getLocation(), AIR_BLOCK_DATA);
        player.spawnParticle(Particle.SMOKE_LARGE, this.position.getLocation(), 20, 0, 0, 0, 2);

        /*
        if (this.model == null) {
            return;
        }

        this.model.hideFromPlayer(player);
         */
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return (object instanceof LootChest lootChest) && lootChest.position.equals(position);
    }

    /**
     * Declares if this loot chest should have its block updated automatically by the ClientLootManager.
     */
    public abstract boolean shouldUpdateDisplay();

    /*
    private void setActiveModel() {
        if (this.model != null) {
            return;
        }

        this.model = ModelEngineAPI.createActiveModel(this.modelID != null ? this.modelID : "chest_wooden");

        if (this.model != null) {
            this.entity.addModel(this.model, false);
        } else {
            RunicCore.getInstance().getLogger().warning("There was an error loading the " + (modelID != null ? modelID : "chest_wooden") + " model!");
        }
    }
     */
}
