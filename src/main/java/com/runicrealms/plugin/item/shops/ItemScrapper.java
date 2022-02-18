package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.plugin.utilities.Tuple;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Item Menu to scrap items
 */
public class ItemScrapper implements RunicShop {

    private static final int SHOP_SIZE = 27;
    private static final String SHOP_NAME = ChatColor.YELLOW + "Item Scrapper";
    public static final Collection<Integer> SCRAPPER_NPC_IDS = Arrays.asList(144, 143, 145, 147, 148, 149, 153, 154, 155);
    public static final Collection<Integer> SCRAPPER_SLOTS = Arrays.asList(10, 11, 12, 13, 14);
    private final InventoryHolder inventoryHolder;
    private final HashMap<UUID, List<ItemStack>> storedItems; // list of items NOT to return

    public ItemScrapper(Player player) {
        this.inventoryHolder = new ItemScrapperHolder(player, SHOP_SIZE, SHOP_NAME);
        storedItems = new HashMap<>();
        List<ItemStack> items = new ArrayList<>();
        storedItems.put(player.getUniqueId(), items);
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
            Tuple<RunicItem, Integer> scrapItems = determineScrappedItems(itemStack);
            if (scrapItems == null) continue;
            storedItems.get(player.getUniqueId()).add(itemStack);
            scrapItems.x.setCount(scrapItems.y);
            RunicItemsAPI.addItem(player.getInventory(), scrapItems.x.generateItem());
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
     * Returns a hashmap with a type of scrap and the amount of scrap to reward
     *
     * @param itemStack to be scrapped
     * @return a list of scrap items to reward
     */
    private static Tuple<RunicItem, Integer> determineScrappedItems(ItemStack itemStack) {
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
        RunicItem runicItemScrap = determineRunicItemScrap(itemStack);
        if (runicItemScrap == null) return null;
        return new Tuple<>(runicItemScrap, determineNumberOfScraps(runicItem));
    }

    /**
     * Determines which type of scrap material to reward based on the material of the scrapped item
     *
     * @param itemStack to be scrapped
     * @return which type of scrap to return
     */
    private static RunicItem determineRunicItemScrap(ItemStack itemStack) {
        switch (itemStack.getType()) {
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case FEATHER:
                return RunicItemsAPI.generateItemFromTemplate("chain-link");
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
            case STONE_SHOVEL:
            case WOODEN_SHOVEL:
            case STONE_HOE:
                return RunicItemsAPI.generateItemFromTemplate("gold-bar");
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
                return RunicItemsAPI.generateItemFromTemplate("Thread");
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                return RunicItemsAPI.generateItemFromTemplate("AnimalHide");
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case SHIELD:
            case WOODEN_AXE:
            case STONE_SWORD:
            case WOODEN_SWORD:
            case BOW:
            case WOODEN_HOE:
                return RunicItemsAPI.generateItemFromTemplate("iron-bar");
            default:
                return null;
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
            return -1; // something went wrong!
        }
        switch (runicItemRarity) {
            case CRAFTED:
            case COMMON:
                return (int) Math.ceil(1 + (itemLevel * 0.1));
            case UNCOMMON:
                return (int) Math.ceil(2 + (itemLevel * 0.15));
            case RARE:
                return (int) Math.ceil(3 + (itemLevel * 0.2));
            case EPIC:
            case LEGENDARY:
            case UNIQUE:
                return (int) Math.ceil(4 + (itemLevel * 0.35));
            default:
                return 1;
        }
    }

    public HashMap<UUID, List<ItemStack>> getStoredItems() {
        return storedItems;
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
            this.inventory.setItem(0, GUIUtil.backButton());
            for (int i = 0; i < SHOP_SIZE; i++) {
                if (SCRAPPER_SLOTS.contains(i)) continue; // skip scrapper slots
                this.inventory.setItem(i, GUIUtil.borderItem());
            }
            this.inventory.setItem(16, checkMark());
            this.inventory.setItem(17, GUIUtil.closeButton());
        }
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
}
