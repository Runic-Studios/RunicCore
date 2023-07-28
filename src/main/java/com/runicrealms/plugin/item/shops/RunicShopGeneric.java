package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class RunicShopGeneric implements RunicItemShop {
    private static final int LOAD_DELAY = 10; // Seconds
    private final int size;
    private final String shopName;
    private final Collection<Integer> runicNpcIds;
    private List<RunicShopItem> itemsForSale;
    private Map<Integer, RunicShopItem> inventoryItems;

    /**
     * REMOVE THIS AFTER THE ORE_VENDER DYNAMIC BUG IS FOUND AND FIXED
     */
    private static void debug(String shopName, boolean init) {
        if (!shopName.equalsIgnoreCase("ore_vender") && !shopName.equalsIgnoreCase("Ore Vendor")) {
            return;
        }

        Thread.dumpStack();
        if (!init) {
            RunicCore.getInstance().getLogger().log(Level.SEVERE, "DYNAMIC ORE_VENDER CHANGE!");
        }
    }

    /**
     * Creates an item shop
     *
     * @param size        of the shop
     * @param shopName    to display
     * @param runicNpcIds that will trigger the shop
     */
    public RunicShopGeneric(int size, String shopName, Collection<Integer> runicNpcIds) {
        this.size = size;
        this.shopName = shopName;
        this.runicNpcIds = runicNpcIds;
        debug(shopName, true); //remove
    }

    /**
     * Creates an item shop
     *
     * @param size         of the shop
     * @param shopName     to display
     * @param runicNpcIds  that will trigger the shop
     * @param itemsForSale a set of items that can be purchased
     */
    public RunicShopGeneric(int size, String shopName, Collection<Integer> runicNpcIds, ArrayList<RunicShopItem> itemsForSale) {
        this.size = size;
        this.shopName = shopName;
        this.runicNpcIds = runicNpcIds;
        setItemsForSale(itemsForSale);
        debug(shopName, true); //remove
    }

    /**
     * Creates an item shop
     *
     * @param size         of the shop
     * @param shopName     to display
     * @param runicNpcIds  that will trigger the shop
     * @param itemsForSale a set of items that can be purchased
     * @param itemSlots    shape of the items. by default, loads them in the GUI left --> right
     */
    public RunicShopGeneric(int size, String shopName, Collection<Integer> runicNpcIds, ArrayList<RunicShopItem> itemsForSale, int[] itemSlots) {
        this.size = size;
        this.shopName = shopName;
        this.runicNpcIds = runicNpcIds;
        this.itemsForSale = itemsForSale;
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            inventoryItems = new HashMap<>();
            int nextItemIndex = 0;
            try {
                for (RunicShopItem runicShopItem : itemsForSale) {
                    inventoryItems.put(itemSlots[nextItemIndex], runicShopItem);
                    nextItemIndex++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().info(ChatColor.DARK_RED + "Error: runic item template id not found!");
            }
            RunicCore.getShopAPI().registerRunicItemShop(this);
        }, LOAD_DELAY * 20L);
        debug(shopName, true); //remove
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
    public ItemStack getIcon() {
        ItemStack vendorItem = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = vendorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(this.getName());
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "View items available for purchase!"));
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

    @Override
    public String getName() {
        return shopName;
    }

    @Override
    public List<RunicShopItem> getItemsForSale() {
        return this.itemsForSale;
    }

    @Override
    public void setItemsForSale(List<RunicShopItem> itemsForSale) {
        this.itemsForSale = itemsForSale;
        registerRunicItemShop();
        debug(shopName, false); //remove
    }

    @Override
    public void registerRunicItemShop() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            inventoryItems = new HashMap<>();
            int nextItemIndex = 0;
            try {
                for (RunicShopItem runicShopItem : itemsForSale) {
                    inventoryItems.put(nextItemIndex, runicShopItem);
                    nextItemIndex++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().info(ChatColor.DARK_RED + "Error: runic item template id not found!");
            }
            RunicCore.getShopAPI().registerRunicItemShop(this);
        }, LOAD_DELAY * 20L);
    }
}
