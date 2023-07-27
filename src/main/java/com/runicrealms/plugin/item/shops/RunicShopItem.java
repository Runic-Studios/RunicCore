package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicitems.item.RunicItemArmor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunicShopItem {
    private final List<Pair<String, Integer>> requiredItems;
    private final ItemStack shopItem;
    private final RunicItemRunnable runicItemRunnable;
    private List<ShopCondition> extraConditions = new ArrayList<>(); // A list of additional requirements to purchase items
    private boolean removePayment = true;

    /**
     * Creates a 'buy item' for the shop!
     *
     * @param requiredItems list of pairs of string name(s) of template ID for RunicItem currency and an amount of that item
     * @param shopItem      to be purchased
     */
    public RunicShopItem(@NotNull List<Pair<String, Integer>> requiredItems, @NotNull ItemStack shopItem) {
        this.requiredItems = requiredItems;
        this.shopItem = shopItem;
        this.runicItemRunnable = player -> {
            // attempt to give player item (does not drop on floor)
            RunicItemsAPI.addItem(player.getInventory(), shopItem, true);
        };
    }

    /**
     * Shorthand constructor for simple shop items that cost only gold coins
     *
     * @param cost of the item in gold coins
     */
    public RunicShopItem(int cost, @NotNull ItemStack shopItem, @NotNull RunicItemRunnable runicItemRunnable) {
        this.requiredItems = Collections.singletonList(Pair.pair("coin", cost));
        this.shopItem = shopItem;
        this.runicItemRunnable = runicItemRunnable;
    }

    /**
     * Creates a 'buy item' for the shop!
     *
     * @param runicItemRunnable a custom runnable to be executed upon item purchase
     */
    public RunicShopItem(@NotNull List<Pair<String, Integer>> requiredItems, @NotNull ItemStack shopItem, @NotNull RunicItemRunnable runicItemRunnable) {
        this.requiredItems = requiredItems;
        this.shopItem = shopItem;
        this.runicItemRunnable = runicItemRunnable;
    }

    /**
     * @param extraConditions a set of extra conditions which must be met to purchase item
     */
    public RunicShopItem(@NotNull List<Pair<String, Integer>> requiredItems, @NotNull ItemStack shopItem,
                         @NotNull RunicItemRunnable runicItemRunnable, List<ShopCondition> extraConditions) {
        this.requiredItems = requiredItems;
        this.shopItem = shopItem;
        this.runicItemRunnable = runicItemRunnable;
        this.extraConditions = extraConditions;
    }

    /**
     * Shorthand constructor for simple shop items that cost only gold coins
     *
     * @param cost of the item in gold coins
     */
    public RunicShopItem(int cost, ItemStack shopItem,
                         RunicItemRunnable runicItemRunnable, List<ShopCondition> extraConditions) {
        this.requiredItems = Collections.singletonList(Pair.pair("coin", cost));
        this.shopItem = shopItem;
        this.runicItemRunnable = runicItemRunnable;
        this.extraConditions = extraConditions;
    }

    /**
     * The generic item shop lore generator that appends the price
     *
     * @param runicShopItem the ShopItem wrapper of the item in the store
     * @return a display-able item with lore like price info
     */
    @NotNull
    public static ItemStack iconWithLore(@NotNull RunicShopItem runicShopItem) {
        ItemStack itemStack = runicShopItem.getShopItem();
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
        if (runicItem instanceof RunicItemArmor runicItemArmor) {
            runicItemArmor.setIsMenuDisplay(true);
            itemStack = runicItemArmor.generateGUIItem();
        }

        if (runicShopItem.isFree()) {
            return itemStack;
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

    public List<ShopCondition> getExtraConditions() {
        return extraConditions;
    }

    public List<String> getPriceLore() {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GOLD + "Price: ");
        for (Pair<String, Integer> pair : this.requiredItems) {
            String templateID = pair.first;
            String displayName = RunicItemsAPI.generateItemFromTemplate(templateID).getDisplayableItem().getDisplayName();
            lore.add(ChatColor.GOLD + "- " + ChatColor.GREEN + ChatColor.BOLD + pair.second + " " + ChatColor.WHITE + displayName);
        }
        return lore;
    }

    /**
     * @return a list of pairs of the runic item id and the cost of the item
     */
    public List<Pair<String, Integer>> getRequiredItems() {
        return requiredItems;
    }

    public ItemStack getShopItem() {
        return shopItem;
    }

    public boolean removePayment() {
        return removePayment;
    }

    /**
     * A method that returns if the only required items are coins and the required amount is less than or equal to zero
     *
     * @return if the only required items are coins and the required amount is less than or equal to zero
     */
    public boolean isFree() {
        return this.requiredItems.size() == 1 && this.requiredItems.get(0).first.equalsIgnoreCase("coin") && this.requiredItems.get(0).second <= 0;
    }

    /*
    MAKE SURE somewhere that there is space in the player's inventory.
     */
    public void runBuy(Player player) {
        runicItemRunnable.run(player);
    }

    public void setRemovePayment(boolean removePayment) {
        this.removePayment = removePayment;
    }
}
