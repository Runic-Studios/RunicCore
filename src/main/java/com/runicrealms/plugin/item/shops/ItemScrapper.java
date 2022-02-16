package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.utilities.GUIUtil;
import com.runicrealms.plugin.utilities.Tuple;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArmor;
import com.runicrealms.runicitems.item.RunicItemOffhand;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.runicrealms.runicitems.item.stats.RunicItemRarity;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Item Menu to scrap items
 */
public class ItemScrapper extends RunicShop {

    private static final int SHOP_SIZE = 27;
    private static final String SHOP_NAME = "&eItem Scrapper";
    public static final Collection<Integer> SCRAPPER_NPC_IDS = Collections.singletonList(144);
    public static final Collection<Integer> SCRAPPER_SLOTS = Arrays.asList(10, 11, 12, 13, 14);
    private final HashMap<UUID, List<ItemStack>> storedItems; // list of items NOT to return

    public ItemScrapper(Player player) {
        super(SHOP_SIZE, new ItemStack(Material.STONE), SHOP_NAME, SCRAPPER_NPC_IDS);
        setupShop(player);
        storedItems = new HashMap<>();
        List<ItemStack> items = new ArrayList<>();
        storedItems.put(player.getUniqueId(), items);
    }

    @Override
    public void setupShop(Player player) {

        super.setupShop(player);
        ItemGUI scrapperMenu = getItemGUI();
        scrapperMenu.setOption(4, new ItemStack(Material.STONE));
        for (int i = 0; i < SHOP_SIZE; i++) {
            if (SCRAPPER_SLOTS.contains(i)) continue; // skip scrapper slots
            scrapperMenu.setOption(i, GUIUtil.borderItem());
        }
        scrapperMenu.setOption(16, new ItemStack(Material.SLIME_BALL),
                "&aScrap Items", "&7Scrap items and receive &6gold&7!", 0, false);
        scrapperMenu.setOption(17, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0, false);

        // set the handler
        scrapperMenu.setHandler(event -> {

            // convert the items to gold
            if (event.getSlot() == 16) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                scrapItems(player, scrapperMenu);
                event.setWillClose(true);
                event.setWillDestroy(true);

                // close editor
            } else if (event.getSlot() == 17) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                event.setWillClose(true);
                event.setWillDestroy(true);
            }
        });

        // update our internal menu
        this.setItemGUI(scrapperMenu);
    }

    /**
     * This method reads the items in the first seven slots of the menu,
     * removes them, and then decides how much gold to dish out.
     *
     * @param player       to give gold to
     * @param scrapperMenu the UI menu
     */
    private void scrapItems(Player player, ItemGUI scrapperMenu) {

        boolean placedValidItem = false;

        // loop through items
        for (Integer slot : SCRAPPER_SLOTS) {
            if (scrapperMenu.getItem(slot) == null) continue;
            ItemStack itemStack = scrapperMenu.getItem(slot);
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
                return (int) Math.ceil(4 + (itemLevel * 0.35));
            default:
                return 1;
        }
    }

    public HashMap<UUID, List<ItemStack>> getStoredItems() {
        return storedItems;
    }
}
