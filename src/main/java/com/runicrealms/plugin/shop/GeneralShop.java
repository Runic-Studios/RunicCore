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

public class GeneralShop implements RunicItemShop {
    private final Map<Integer, RunicShopItem> availableItems;

    private static final int LOAD_DELAY = 10;

    public GeneralShop() {
        this.availableItems = new HashMap<>();
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            int nextItemIndex = 0;
            nextItemIndex = loadRunicArmor(nextItemIndex, 0,
                    "crystal-cavern-archer-helm",
                    "crystal-cavern-archer-chest",
                    "crystal-cavern-archer-leggings",
                    "crystal-cavern-archer-boots"); //placeholder
            RunicCoreAPI.registerRunicItemShop(this);
        }, LOAD_DELAY * 20L);
    }

    /**
     * Handy method for loading runic items from their template id.
     * @param nextItemIndex the index to start adding armor vertically
     * @param templateIds the string id of the armor piece
     * @return the next index to start adding items
     */
    private int loadRunicArmor(int nextItemIndex, int price, String... templateIds) {
        int temp = nextItemIndex;
        for (String s : templateIds) {
            try {
                ItemStack itemStack = RunicItemsAPI.generateItemFromTemplate(s).generateItem();
                this.availableItems.put(nextItemIndex, new RunicShopItem(price, "Coin",
                        this.iconWithLore(itemStack, price), this.runShopBuy(itemStack)));
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
        return this.availableItems;
    }

    /**
     * Size of the shop minus the title row
     * @return size of shop minus title row (smallest size 9)
     */
    @Override
    public int getShopSize() {
        return 36;
    }

    @Override
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(Material.DIRT); //placeholder
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(this.getName());
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "Purchase items for your adventure!"));
            icon.setItemMeta(meta);
        }
        return icon;
    }

    /**
     * From RunicNPCS
     * @return ID of NPC in config
     */
    @Override
    public Collection<Integer> getNpcIds() {
        return Collections.singletonList(69); //placeholder
    }

    @Override
    public String getName() {
        return ChatColor.YELLOW + "General Shop";
    }

    private RunicItemRunnable runShopBuy(ItemStack item) {
        return player -> {
            // attempt to give player item (does not drop on floor)
            player.getInventory().addItem(item);
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
                            price + " Coin(s)"
            );
            meta.setLore(lore);
            iconWithLore.setItemMeta(meta);
        }
        return iconWithLore;
    }
}
