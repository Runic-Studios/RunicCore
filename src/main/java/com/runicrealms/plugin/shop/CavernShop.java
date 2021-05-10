package com.runicrealms.plugin.shop;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.item.shops.RunicItemRunnable;
import com.runicrealms.plugin.item.shops.RunicItemShop;
import com.runicrealms.plugin.item.shops.RunicShopItem;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CavernShop implements RunicItemShop {

    private static final int ARMOR_PRICE = 2;
    private static final int LOAD_DELAY = 10;
    private static final String MYTHIC_CURRENCY = "HeadOfHexagonis"; // todo: update to runicitems
    private Map<Integer, RunicShopItem> availableItems;

    public CavernShop() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            availableItems = new HashMap<>();
            int nextItemIndex = 0;
            nextItemIndex = loadRunicArmor(nextItemIndex,
                    "crystal-cavern-archer-helmet",
                    "crystal-cavern-archer-chest",
                    "crystal-cavern-archer-leggings",
                    "crystal-cavern-archer-boots");
            nextItemIndex = loadRunicArmor(nextItemIndex,
                    "crystal-cavern-cleric-helmet",
                    "crystal-cavern-cleric-chest",
                    "crystal-cavern-cleric-leggings",
                    "crystal-cavern-cleric-boots");
            nextItemIndex = loadRunicArmor(nextItemIndex,
                    "crystal-cavern-mage-helmet",
                    "crystal-cavern-mage-chest",
                    "crystal-cavern-mage-leggings",
                    "crystal-cavern-mage-boots");
            nextItemIndex = loadRunicArmor(nextItemIndex,
                    "crystal-cavern-rogue-helmet",
                    "crystal-cavern-rogue-chest",
                    "crystal-cavern-rogue-leggings",
                    "crystal-cavern-rogue-boots");
            loadRunicArmor(nextItemIndex,
                    "crystal-cavern-warrior-helmet",
                    "crystal-cavern-warrior-chest",
                    "crystal-cavern-warrior-leggings",
                    "crystal-cavern-warrior-boots");
            RunicCoreAPI.registerRunicItemShop(this);
        }, LOAD_DELAY * 20L);
    }

    /**
     * Handy method for loading runic items from their template id.
     * @param nextItemIndex the index to start adding armor vertically
     * @param templateIds the string id of the armor piece
     * @return the next index to start adding items
     */
    private int loadRunicArmor(int nextItemIndex, String... templateIds) {
        int temp = nextItemIndex;
        for (String s : templateIds) {
            try {
                ItemStack itemStack = RunicItemsAPI.generateItemFromTemplate(s).generateItem();
                availableItems.put(nextItemIndex, new RunicShopItem(ARMOR_PRICE, MYTHIC_CURRENCY,
                        iconWithLore(itemStack, ARMOR_PRICE), runShopBuy(itemStack)));
                nextItemIndex += 9;
            } catch (Exception e) {
                Bukkit.getLogger().info(ChatColor.DARK_RED + "Error: runic item template id not found!");
                e.printStackTrace();
            }
        }
        return temp + 1;
    }

    @Override
    public Map<Integer, RunicShopItem> getContents() {
        return availableItems;
    }

    /**
     * Size of the shop minus the title row
     * @return size of shop minus title row (smallest size 9)
     */
    @Override
    public int getShopSize() {
        return 52;
    }

    @Override
    public ItemStack getIcon() {
        ItemStack vendorItem = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta meta = vendorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Crystal Cavern Dungeon Shop");
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Purchase items for your valor!"));
            vendorItem.setItemMeta(meta);
        }
        return vendorItem;
    }

    /**
     * From RunicNPCS
     * @return ID of NPC in config
     */
    @Override
    public Collection<Integer> getNpcIds() {
        return Collections.singletonList(32);
    }

    @Override
    public String getName() {
        return ChatColor.YELLOW + "Crystal Cavern Dungeon Shop";
    }

    private RunicItemRunnable runShopBuy(ItemStack tierSetItem) {
        return player -> {
            // attempt to give player item (does not drop on floor)
            player.getInventory().addItem(tierSetItem);
        };
    }

    private ItemStack iconWithLore(ItemStack is, int price) {
        ItemStack iconWithLore = is.clone();
        ItemMeta meta = iconWithLore.getItemMeta();
        if (meta != null && meta.getLore() != null) {
            List<String> lore = meta.getLore();
            lore.add("");
            lore.add(
                    ChatColor.GOLD + "Price: " +
                            ChatColor.GREEN + ChatColor.BOLD +
                            price + " Head(s) of Hexagonis"
            );
            meta.setLore(lore);
            iconWithLore.setItemMeta(meta);
        }
        return iconWithLore;
    }
}
