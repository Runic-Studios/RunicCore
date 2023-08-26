package com.runicrealms.plugin.loot.chest;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.loot.LootHolder;
import com.runicrealms.plugin.resourcepack.ResourcePackManager;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LootChest implements LootHolder {

    protected static final BlockData AIR_BLOCK_DATA = Material.AIR.createBlockData();
    protected static final BlockData BARRIER_BLOCK_DATA = Material.BARRIER.createBlockData();

    protected final LootChestPosition position;
    protected final LootChestTemplate lootChestTemplate;
    protected final LootChestConditions conditions;
    protected final int minLevel;
    protected final int itemMinLevel;
    protected final int itemMaxLevel;
    protected final String inventoryTitle;

    protected final ModeledEntity entity;
    private final String modelID;
    protected ActiveModel model;

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

        this.modelID = modelID;

        Dummy dummy = ModelEngineAPI.createDummy();

        Location target = position.getLocation().clone();
        target.setX(target.getX() + .5);
        target.setZ(target.getZ() + .5);

        target.setDirection(this.position.getDirection().getDirection());

        dummy.setLocation(target);
        dummy.setYHeadRot(target.getYaw());
        dummy.setYBodyRot(target.getYaw());

        this.entity = ModelEngineAPI.createModeledEntity(dummy);
        this.entity.getRangeManager().setRenderDistance(0);

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
        //Bukkit.broadcastMessage("open inventory"); //remove
    }

    public void playOpenAnimation() {
        this.setActiveModel();
        this.model.getAnimationHandler().playAnimation("hit", 0, 0, 2, true);
    }

    public void showToPlayer(@NotNull Player player) {
        player.sendBlockChange(this.position.getLocation(), ResourcePackManager.isPackActive(player) ? BARRIER_BLOCK_DATA : this.blockData);
        //Bukkit.broadcastMessage("show chest"); //remove

        this.setActiveModel();

        if (this.model == null || !ResourcePackManager.isPackActive(player)) {
            return;
        }

        this.entity.getRangeManager().forceSpawn(player);
    }

    public void hideFromPlayer(@NotNull Player player) {
        player.sendBlockChange(this.position.getLocation(), AIR_BLOCK_DATA);
        player.spawnParticle(Particle.REDSTONE, this.position.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.WHITE, 20));
        //Bukkit.broadcastMessage("hide chest"); //remove

        if (this.model == null || !ResourcePackManager.isPackActive(player)) {
            return;
        }

        this.entity.getRangeManager().removePlayer(player);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        return (object instanceof LootChest lootChest) && lootChest.position.equals(position);
    }

    /**
     * Declares if this loot chest should have its block updated automatically by the ClientLootManager.
     */
    public abstract boolean shouldUpdateDisplay();

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
}
