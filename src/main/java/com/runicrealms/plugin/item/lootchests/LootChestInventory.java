package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootChestInventory implements InventoryHolder {

    private static final Random random = new Random();
    private static final int CHEST_SIZE = 27;
    private final int minItems;
    private final int maxItems;
    private final Inventory inventory;
    private final LootChestTier lootChestTier;
    private final Player player;

    public LootChestInventory(Player player, LootChestTier lootChestTier) {
        this.player = player;
        this.lootChestTier = lootChestTier;
        this.minItems = lootChestTier.getMinimumItems();
        this.maxItems = lootChestTier.getMaximumItems();
        this.inventory = Bukkit.createInventory(this, CHEST_SIZE, lootChestTier.getTitle());
        setupChestInventory();
    }

    /**
     * Fills the chest inventory with items from the drop table
     */
    private void fillInventory() {
        int numberOfItems = random.nextInt(maxItems - minItems) + minItems;
        ChestItem chestItem;
        List<Integer> used = new ArrayList<>();
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
            switch (lootChestTier) {
                case TIER_II:
                    chestItem = RunicCore.getLootTableAPI().getLootTableTierII().getRandom();
                    break;
                case TIER_III:
                    chestItem = RunicCore.getLootTableAPI().getLootTableTierIII().getRandom();
                    break;
                case TIER_IV:
                    chestItem = RunicCore.getLootTableAPI().getLootTableTierIV().getRandom();
                    break;
                default:
                    chestItem = RunicCore.getLootTableAPI().getLootTableTierI().getRandom();
                    break;
            }
            ItemStack itemStack = RunicCore.getLootTableAPI().generateItemStack(chestItem, lootChestTier);
            this.inventory.setItem(slot, itemStack);
        }
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