package com.runicrealms.plugin.item.shops;

import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemArmor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RunicShopItem {

    private final Map<String, Integer> requiredItems;
    private final ItemStack shopItem;
    private final RunicItemRunnable runicItemRunnable;
    private boolean removePayment = true;

    /**
     * Creates a 'buy item' for the shop!
     *
     * @param requiredItems Map of string name(s) of template ID for RunicItem currency to an amount of that item
     * @param shopItem      to be purchased
     */
    public RunicShopItem(Map<String, Integer> requiredItems, ItemStack shopItem) {
        this.requiredItems = requiredItems;
        this.shopItem = shopItem;
        this.runicItemRunnable = runDefaultBuy();
    }

    /**
     * Creates a 'buy item' for the shop!
     *
     * @param runicItemRunnable a custom runnable to be executed upon item purchase
     */
    public RunicShopItem(Map<String, Integer> requiredItems, ItemStack shopItem, RunicItemRunnable runicItemRunnable) {
        this.requiredItems = requiredItems;
        this.shopItem = shopItem;
        this.runicItemRunnable = runicItemRunnable;
    }

    /**
     * The generic item shop lore generator that appends the price
     *
     * @param runicShopItem the ShopItem wrapper of the item in the store
     * @return a display-able item with lore like price info
     */
    public static ItemStack iconWithLore(RunicShopItem runicShopItem) {
        ItemStack itemStack = runicShopItem.getShopItem();
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
        if (runicItem instanceof RunicItemArmor runicItemArmor) {
            runicItemArmor.setIsMenuDisplay(true);
            itemStack = runicItemArmor.generateGUIItem();
        }
        ItemStack iconWithLore = itemStack.clone();
        ItemMeta meta = iconWithLore.getItemMeta();
        assert meta != null;
        List<String> lore = meta.getLore();
        assert lore != null;
        lore.addAll(runicShopItem.getPriceLore());
        meta.setLore(lore);
        iconWithLore.setItemMeta(meta);
        return iconWithLore;
    }

    /**
     * @return
     */
    public List<String> getPriceLore() {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "Price: ");
        for (String templateID : this.requiredItems.keySet()) {
            String displayName = RunicItemsAPI.generateItemFromTemplate(templateID).getDisplayableItem().getDisplayName();
            lore.add(ChatColor.GOLD + "- " + ChatColor.GREEN + ChatColor.BOLD + this.requiredItems.get(templateID) + " " + displayName);
        }
        return lore;
    }

    /**
     * @return
     */
    public Map<String, Integer> getRequiredItems() {
        return requiredItems;
    }

    public ItemStack getShopItem() {
        return shopItem;
    }

    public boolean removePayment() {
        return removePayment;
    }

    /*
    MAKE SURE somewhere that there is space in the player's inventory.
     */
    public void runBuy(Player player) {
        runicItemRunnable.run(player);
    }

    private RunicItemRunnable runDefaultBuy() {
        return player -> {
            // attempt to give player item (does not drop on floor)
            RunicItemsAPI.addItem(player.getInventory(), shopItem, true);
        };
    }

    public void setRemovePayment(boolean removePayment) {
        this.removePayment = removePayment;
    }
}
