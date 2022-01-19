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
    private final LootChestRarity lootChestRarity;
    private final Player player;

    private static final Random random = new Random();
    private static final int CHEST_SIZE = 27;

    public LootChestInventory(Player player, LootChestRarity lootChestRarity) {
        this.player = player;
        this.lootChestRarity = lootChestRarity;
        this.minItems = lootChestRarity.getMinimumItems();
        this.maxItems = lootChestRarity.getMaximumItems();
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

    public LootChestRarity getLootChestRarity() {
        return lootChestRarity;
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
        switch (this.lootChestRarity) {
            case UNCOMMON:
                return "&f&l" + player.getName() + "'s &a&lUncommon Chest";
            case RARE:
                return "&f&l" + player.getName() + "'s &b&lRare Chest";
            case EPIC:
                return "&f&l" + player.getName() + "'s &d&lEpic Chest";
            default:
                return "&f&l" + player.getName() + "'s &7&lCommon Chest";
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
            switch (lootChestRarity) {
                case UNCOMMON:
                    chestItem = ChestLootTableUtil.uncommonLootTable().getRandom();
                    break;
                case RARE:
                    chestItem = ChestLootTableUtil.rareLootTable().getRandom();
                    break;
                case EPIC:
                    chestItem = ChestLootTableUtil.epicLootTable().getRandom();
                    break;
                default:
                    chestItem = ChestLootTableUtil.commonLootTable().getRandom();
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