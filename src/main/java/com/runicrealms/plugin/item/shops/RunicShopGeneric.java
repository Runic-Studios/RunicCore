package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RunicShopGeneric implements RunicItemShop {

    private static final int LOAD_DELAY = 10;
    private final int size;
    private final String shopName;
    private final Collection<Integer> runicNpcIds;
    private final LinkedHashMap<ItemStack, RunicShopItem> itemsForSale;
    private Map<Integer, RunicShopItem> inventoryItems;

    /**
     * This...
     *
     * @param size
     * @param shopName
     * @param runicNpcIds
     * @param itemsForSale
     */
    public RunicShopGeneric(int size, String shopName, Collection<Integer> runicNpcIds, LinkedHashMap<ItemStack, RunicShopItem> itemsForSale) {
        this.size = size;
        this.shopName = shopName;
        this.runicNpcIds = runicNpcIds;
        this.itemsForSale = itemsForSale;
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            inventoryItems = new HashMap<>();
            int nextItemIndex = 0;
            try {
                for (Map.Entry<ItemStack, RunicShopItem> entry : itemsForSale.entrySet()) {
                    inventoryItems.put(nextItemIndex, entry.getValue());
                    nextItemIndex++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().info(ChatColor.DARK_RED + "Error: runic item template id not found!");
            }
            RunicCoreAPI.registerRunicItemShop(this);
        }, LOAD_DELAY * 20L);
    }

    /**
     * This...
     *
     * @param size
     * @param shopName
     * @param runicNpcIds
     * @param itemsForSale
     * @param itemSlots
     */
    public RunicShopGeneric(int size, String shopName, Collection<Integer> runicNpcIds, LinkedHashMap<ItemStack, RunicShopItem> itemsForSale, int[] itemSlots) {
        this.size = size;
        this.shopName = shopName;
        this.runicNpcIds = runicNpcIds;
        this.itemsForSale = itemsForSale;
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            inventoryItems = new HashMap<>();
            int nextItemIndex = 0;
            try {
                for (Map.Entry<ItemStack, RunicShopItem> entry : itemsForSale.entrySet()) {
                    inventoryItems.put(itemSlots[nextItemIndex], entry.getValue());
                    nextItemIndex++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().info(ChatColor.DARK_RED + "Error: runic item template id not found!");
            }
            RunicCoreAPI.registerRunicItemShop(this);
        }, LOAD_DELAY * 20L);
    }

    @Override
    public Map<Integer, RunicShopItem> getContents() {
        return inventoryItems;
    }

    /**
     * Size of the shop minus the title row
     *
     * @return size of shop minus title row (smallest size 9)
     */
    @Override
    public int getShopSize() {
        return this.size;
    }


    @Override
    public String getName() {
        return shopName;
    }

    @Override
    public ItemStack getIcon() {
        ItemStack vendorItem = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta meta = vendorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(this.getName());
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Purchase items for your valor!"));
            vendorItem.setItemMeta(meta);
        }
        return vendorItem;
    }

    /**
     * From RunicNPCS
     *
     * @return ID of NPC in config
     */
    @Override
    public Collection<Integer> getRunicNpcIds() {
        return this.runicNpcIds;
    }

    public Map<ItemStack, RunicShopItem> getItemsForSale() {
        return this.itemsForSale;
    }

    /**
     * The generic item shop lore generator to be used if coins are the price
     *
     * @param is itemstack to be sold
     * @param price of the item
     * @return a display-able item with lore like price info
     */
    public static ItemStack iconWithLore(ItemStack is, int price) {
        ItemStack iconWithLore = is.clone();
        ItemMeta meta = iconWithLore.getItemMeta();
        if (meta != null && meta.getLore() != null) {
            List<String> lore = meta.getLore();
            lore.add("");
            lore.add(
                    ChatColor.GOLD + "Price: " +
                            ChatColor.GREEN + ChatColor.BOLD +
                            price + " " + CurrencyUtil.goldCoin().getItemMeta().getDisplayName()
            );
            meta.setLore(lore);
            iconWithLore.setItemMeta(meta);
        }
        return iconWithLore;
    }

    public static ItemStack iconWithLore(ItemStack is, String priceItemDisplayName) {
        ItemStack iconWithLore = is.clone();
        ItemMeta meta = iconWithLore.getItemMeta();
        if (meta != null && meta.getLore() != null) {
            List<String> lore = meta.getLore();
            lore.add("");
            lore.add(
                    ChatColor.GOLD + "Price: " +
                            ChatColor.GREEN + ChatColor.BOLD +
                            priceItemDisplayName
            );
            meta.setLore(lore);
            iconWithLore.setItemMeta(meta);
        }
        return iconWithLore;
    }

    public static ItemStack iconWithLore(ItemStack is, int price, String priceItemDisplayName) {
        ItemStack iconWithLore = is.clone();
        ItemMeta meta = iconWithLore.getItemMeta();
        if (meta != null && meta.getLore() != null) {
            List<String> lore = meta.getLore();
            lore.add("");
            lore.add(
                    ChatColor.GOLD + "Price: " +
                            ChatColor.GREEN + ChatColor.BOLD +
                            price + " " + priceItemDisplayName
            );
            meta.setLore(lore);
            iconWithLore.setItemMeta(meta);
        }
        return iconWithLore;
    }
}
