package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BossChestInventory implements InventoryHolder {
    private static final Random random = new Random();
    private static final int CHEST_SIZE = 27;
    private final int minItems;
    private final int maxItems;
    private final Inventory inventory;
    private final Player player;
    private final DungeonLocation dungeonLocation;

    /**
     * @param player          who opened the boss chest
     * @param dungeonLocation the dungeon of the chest
     */
    public BossChestInventory(Player player, DungeonLocation dungeonLocation) {
        this.player = player;
        this.dungeonLocation = dungeonLocation;
        this.minItems = dungeonLocation.getBossChestTier().getMinimumItems();
        this.maxItems = dungeonLocation.getBossChestTier().getMaximumItems();
        this.inventory = Bukkit.createInventory(this, CHEST_SIZE, ChatColor.GOLD + "" + ChatColor.BOLD + dungeonLocation.getDisplay() + " Spoils");
        setupChestInventory();
    }

    /**
     * Fills the chest inventory with items from the drop table
     */
    private void fillInventory() {
        int numberOfItems = random.nextInt(maxItems - minItems) + minItems;
        ChestItem chestItem;
        List<Integer> used = new ArrayList<>();
        BossChestTier bossChestTier = dungeonLocation.getBossChestTier();
        for (int i = 0; i < numberOfItems; i++) {

            // prevent items overriding the same slot
            int slot = random.nextInt(26);
            while (used.contains(slot)) {
                slot = random.nextInt(26);
            }
            if (!used.contains(slot)) {
                used.add(slot);
            }

            // fill inventory
            switch (bossChestTier) {
                case CRYSTAL_CAVERN:
                    chestItem = RunicCore.getLootTableAPI().getLootTableCrystalCavern().getRandom();
                    break;
                case JORUNDRS_KEEP:
                    chestItem = RunicCore.getLootTableAPI().getLootTableJorundrsKeep().getRandom();
                    break;
                case SUNKEN_LIBRARY:
                    chestItem = RunicCore.getLootTableAPI().getLootTableSunkenLibrary().getRandom();
                    break;
                case CRYPTS_OF_DERA:
                    chestItem = RunicCore.getLootTableAPI().getLootTableCryptsOfDera().getRandom();
                    break;
                case FROZEN_FORTRESS:
                    chestItem = RunicCore.getLootTableAPI().getLootTableFrozenFortress().getRandom();
                    break;
                default:
                    chestItem = RunicCore.getLootTableAPI().getLootTableSebathsCave().getRandom();
                    break;
            }
            ItemStack itemStack = RunicCore.getLootTableAPI().generateItemStack(chestItem, bossChestTier);
            this.inventory.setItem(slot, itemStack);
        }
        // Add a dungeon token to every inventory
        this.inventory.setItem(this.getInventory().firstEmpty(), RunicItemsAPI.generateItemFromTemplate(dungeonLocation.getCurrencyTemplateId()).generateItem());
    }

    public DungeonLocation getDungeonLocation() {
        return dungeonLocation;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Opens the inventory associated w/ this GUI
     */
    private void setupChestInventory() {
        this.inventory.clear();
        fillInventory();
    }
}