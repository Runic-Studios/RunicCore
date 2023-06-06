package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.ItemType;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Item Menu to scrap items
 */
public class ItemScrapper implements RunicShop {
    public static final Collection<Integer> SCRAPPER_NPC_IDS = Arrays.asList(144, 143, 145, 147, 148, 149, 151, 152, 153, 154, 155, 727);
    public static final Collection<Integer> SCRAPPER_SLOTS = Arrays.asList(10, 11, 12, 13, 14);
    private static final int SHOP_SIZE = 27;
    private static final String SHOP_NAME = ChatColor.YELLOW + "Item Scrapper";
    private final InventoryHolder inventoryHolder;
    private final HashMap<UUID, List<ItemStack>> storedItems; // List of items NOT to return (invalid items)

    public ItemScrapper(Player player) {
        this.inventoryHolder = new ItemScrapperHolder(player, SHOP_SIZE, SHOP_NAME);
        storedItems = new HashMap<>();
        List<ItemStack> items = new ArrayList<>();
        storedItems.put(player.getUniqueId(), items);
    }

    /**
     * Returns a hashmap with a type of scrap and the amount of scrap to reward
     *
     * @param itemStack to be scrapped
     * @return a list of scrap items to reward
     */
    private static Pair<RunicItem, Integer> determineScrappedItems(ItemStack itemStack) {
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
        RunicItem runicItemScrap = determineRunicItemScrap(itemStack);
        if (runicItemScrap == null) return null;
        return new Pair<>(runicItemScrap, determineNumberOfScraps(runicItem));
    }

    /**
     * Determines which type of scrap material to reward based on the material of the scrapped item
     *
     * @param itemStack to be scrapped
     * @return which type of scrap to return
     */
    private static RunicItem determineRunicItemScrap(ItemStack itemStack) {
        switch (itemStack.getType()) {
            case CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS, FEATHER -> {
                return RunicItemsAPI.generateItemFromTemplate("chain-link");
            }
            case GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS, STONE_SHOVEL, WOODEN_SHOVEL, STONE_HOE -> {
                return RunicItemsAPI.generateItemFromTemplate("gold-bar");
            }
            case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> {
                return RunicItemsAPI.generateItemFromTemplate("thread");
            }
            case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> {
                return RunicItemsAPI.generateItemFromTemplate("animal-hide");
            }
            case IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS, SHIELD, WOODEN_AXE, STONE_SWORD, WOODEN_SWORD, BOW, WOODEN_HOE -> {
                return RunicItemsAPI.generateItemFromTemplate("iron-bar");
            }
            // Offhands made from shears
            case SHEARS -> {
                if (itemStack.getItemMeta() == null) return null;
                if (ItemType.OFFHAND_ITEM_IDS.contains(((Damageable) itemStack.getItemMeta()).getDamage()))
                    return RunicItemsAPI.generateItemFromTemplate("iron-bar");
                else
                    return null;
            }
            default -> {
                return null;
            }
        }
    }

    /**
     * Determines the number of scraps to reward based on the rarity and level of the scrapped RunicItem
     *
     * @param runicItem to be scrapped
     * @return number of scraps to reward
     */
    private static Integer determineNumberOfScraps(RunicItem runicItem) {
        RunicItemRarity runicItemRarity;
        int itemLevel;
        if (runicItem instanceof RunicItemArmor) {
            runicItemRarity = ((RunicItemArmor) runicItem).getRarity();
            itemLevel = ((RunicItemArmor) runicItem).getLevel();
        } else if (runicItem instanceof RunicItemWeapon) {
            runicItemRarity = ((RunicItemWeapon) runicItem).getRarity();
            itemLevel = ((RunicItemWeapon) runicItem).getLevel();
        } else if (runicItem instanceof RunicItemOffhand) {
            runicItemRarity = ((RunicItemOffhand) runicItem).getRarity();
            itemLevel = ((RunicItemOffhand) runicItem).getLevel();
        } else {
            return -1; // Something went wrong!
        }
        return switch (runicItemRarity) {
            case CRAFTED, COMMON -> (int) Math.ceil(1 + (itemLevel * 0.1));
            case UNCOMMON -> (int) Math.ceil(2 + (itemLevel * 0.15));
            case RARE -> (int) Math.ceil(3 + (itemLevel * 0.2));
            case EPIC, LEGENDARY, UNIQUE -> (int) Math.ceil(4 + (itemLevel * 0.35));
            default -> 1;
        };
    }

    public static ItemStack checkMark() {
        ItemStack item = new ItemStack(Material.SLIME_BALL, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ColorUtil.format("&aScrap Items"));
        meta.setLore(Collections.singletonList(ColorUtil.format("&7Scrap items and receive &ecrafting reagents&7!")));
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public int getShopSize() {
        return SHOP_SIZE;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Material.STONE);
    }

    @Override
    public String getName() {
        return SHOP_NAME;
    }

    @Override
    public Collection<Integer> getRunicNpcIds() {
        return SCRAPPER_NPC_IDS;
    }

    @Override
    public InventoryHolder getInventoryHolder() {
        return inventoryHolder;
    }

    public HashMap<UUID, List<ItemStack>> getStoredItems() {
        return storedItems;
    }

    /**
     * This method reads the items in the first seven slots of the menu,
     * removes them, and then decides how much gold to dish out.
     *
     * @param player to give gold to
     */
    public void scrapItems(Player player) {

        Inventory inventory = this.getInventoryHolder().getInventory();
        boolean placedValidItem = false;

        // loop through items
        for (Integer slot : SCRAPPER_SLOTS) {
            if (inventory.getItem(slot) == null) continue;
            ItemStack itemStack = inventory.getItem(slot);
            Pair<RunicItem, Integer> scrapItems = determineScrappedItems(itemStack);
            if (scrapItems == null) continue;
            storedItems.get(player.getUniqueId()).add(itemStack);
            scrapItems.first.setCount(scrapItems.second);
            RunicItemsAPI.addItem(player.getInventory(), scrapItems.first.generateItem());
            placedValidItem = true;
        }

        if (placedValidItem) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.GREEN + "You received scraps for your item(s)!");
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.GRAY + "Place an armor piece or weapon inside the menu to scrap it!");
        }

        player.closeInventory();
    }

    /**
     *
     */
    static class ItemScrapperHolder implements InventoryHolder {

        private final Inventory inventory;
        private final Player player;

        public ItemScrapperHolder(Player player, int size, String title) {
            this.inventory = Bukkit.createInventory(this, size, title);
            this.player = player;
            setupInventory();
        }

        @NotNull
        @Override
        public Inventory getInventory() {
            return this.inventory;
        }

        public Player getPlayer() {
            return this.player;
        }

        /**
         * Opens the inventory associated w/ this GUI, ordering perks
         */
        private void setupInventory() {
            this.inventory.clear();
            this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
            for (int i = 0; i < SHOP_SIZE; i++) {
                if (SCRAPPER_SLOTS.contains(i)) continue; // skip scrapper slots
                this.inventory.setItem(i, GUIUtil.BORDER_ITEM);
            }
            this.inventory.setItem(16, checkMark());
            this.inventory.setItem(17, GUIUtil.CLOSE_BUTTON);
        }
    }
}
