package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.utilities.ColorUtil;
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

    private final int minItems;
    private final int maxItems;
    private final Inventory inventory;
    private final LootChestTier lootChestTier;
    private final Player player;

    private static final Random random = new Random();
    private static final int CHEST_SIZE = 27;

    public LootChestInventory(Player player, LootChestTier lootChestTier) {
        this.player = player;
        this.lootChestTier = lootChestTier;
        this.minItems = lootChestTier.getMinimumItems();
        this.maxItems = lootChestTier.getMaximumItems();
        this.inventory = Bukkit.createInventory(this, CHEST_SIZE, ColorUtil.format(chestTitle()));
        openMenu();
    }

    public int getMinItems() {
        return minItems;
    }

    public int getMaxItems() {
        return maxItems;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public LootChestTier getLootChestRarity() {
        return lootChestTier;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Determine the inventory title based on rarity
     *
     * @return a string to be used for the inventory title
     */
    private String chestTitle() {
        switch (this.lootChestTier) {
            case TIER_II:
                return "&f&l" + player.getName() + "'s &a&lTier II Chest";
            case TIER_III:
                return "&f&l" + player.getName() + "'s &b&lTier III Chest";
            case TIER_IV:
                return "&f&l" + player.getName() + "'s &d&lTier IV Chest";
            default:
                return "&f&l" + player.getName() + "'s &7&lTier I Chest";
        }
    }

    /**
     *
     */
    private void fillInventory() {
        int numOfItems = random.nextInt(maxItems - minItems) + minItems;
        ItemStack chestItem;
        List<Integer> used = new ArrayList<>();
        for (int i = 0; i < numOfItems; i++) {

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
                    chestItem = ChestLootTableUtil.lootTableTierII().getRandom();
                    break;
                case TIER_III:
                    chestItem = ChestLootTableUtil.lootTableTierIII().getRandom();
                    break;
                case TIER_IV:
                    chestItem = ChestLootTableUtil.lootTableTierIV().getRandom();
                    break;
                default:
                    chestItem = ChestLootTableUtil.lootTableTierI().getRandom();
                    break;
            }
            this.inventory.setItem(slot, chestItem);
        }
    }

    /**
     * Opens the inventory associated w/ this GUI
     */
    private void openMenu() {
        this.inventory.clear();
        fillInventory();
    }
}