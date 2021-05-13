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

public class FortressShop implements RunicItemShop {

    private static final int ARMOR_PRICE = 5;
    private static final int ARTIFACT_PRICE = 3;
    private static final int LOAD_DELAY = 10;
    private static final String MYTHIC_CURRENCY = "HeadOfEldrid";
    private Map<Integer, RunicShopItem> availableItems;

    public FortressShop() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            availableItems = new HashMap<>();
            int nextItemIndex = 0;
            try {
                ItemStack bow = RunicItemsAPI.generateItemFromTemplate("winters-howl").generateItem();
                availableItems.put(nextItemIndex++, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                        iconWithLore(bow, ARTIFACT_PRICE), runShopBuy(bow)));
                ItemStack mace = RunicItemsAPI.generateItemFromTemplate("chillrend").generateItem();
                availableItems.put(nextItemIndex++, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                        iconWithLore(mace, ARTIFACT_PRICE), runShopBuy(mace)));
                ItemStack staff = RunicItemsAPI.generateItemFromTemplate("permafrost").generateItem();
                availableItems.put(nextItemIndex++, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                        iconWithLore(staff, ARTIFACT_PRICE), runShopBuy(staff)));
                ItemStack sword = RunicItemsAPI.generateItemFromTemplate("blade-of-the-betrayer").generateItem();
                availableItems.put(nextItemIndex++, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                        iconWithLore(sword, ARTIFACT_PRICE), runShopBuy(sword)));
                ItemStack axe = RunicItemsAPI.generateItemFromTemplate("frosts-edge").generateItem();
                availableItems.put(nextItemIndex, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                        iconWithLore(axe, ARTIFACT_PRICE), runShopBuy(axe)));
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getLogger().info(ChatColor.DARK_RED + "Error: runic item template id not found!");
            }
            nextItemIndex = 9;
            nextItemIndex = loadRunicArmor(nextItemIndex,
                    "frozen-fortress-archer-helm",
                    "frozen-fortress-archer-chest",
                    "frozen-fortress-archer-leggings",
                    "frozen-fortress-archer-boots");
            nextItemIndex = loadRunicArmor(nextItemIndex,
                    "frozen-fortress-cleric-helm",
                    "frozen-fortress-cleric-chest",
                    "frozen-fortress-cleric-leggings",
                    "frozen-fortress-cleric-boots");
            nextItemIndex = loadRunicArmor(nextItemIndex,
                    "frozen-fortress-mage-helm",
                    "frozen-fortress-mage-chest",
                    "frozen-fortress-mage-leggings",
                    "frozen-fortress-mage-boots");
            nextItemIndex = loadRunicArmor(nextItemIndex,
                    "frozen-fortress-rogue-helm",
                    "frozen-fortress-rogue-chest",
                    "frozen-fortress-rogue-leggings",
                    "frozen-fortress-rogue-boots");
            loadRunicArmor(nextItemIndex,
                    "frozen-fortress-warrior-helm",
                    "frozen-fortress-warrior-chest",
                    "frozen-fortress-warrior-leggings",
                    "frozen-fortress-warrior-boots");
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
        return 45;
    }

    @Override
    public ItemStack getIcon() {
        ItemStack vendorItem = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta meta = vendorItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + "Frozen Fortress Dungeon Shop");
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
        return Collections.singletonList(31);
    }

    @Override
    public String getName() {
        return ChatColor.YELLOW + "Frozen Fortress Dungeon Shop";
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
                            price + " Head(s) of Eldrid"
            );
            meta.setLore(lore);
            iconWithLore.setItemMeta(meta);
        }
        return iconWithLore;
    }
}
