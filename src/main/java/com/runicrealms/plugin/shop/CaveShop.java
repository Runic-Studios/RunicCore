package com.runicrealms.plugin.shop;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.item.shops.RunicItemRunnable;
import com.runicrealms.plugin.item.shops.RunicItemShop;
import com.runicrealms.plugin.item.shops.RunicShopItem;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CaveShop implements RunicItemShop {

    private static final int ARMOR_PRICE = 1;
    private static final int ARTIFACT_PRICE = 2;
    private static final String MYTHIC_CURRENCY = "HeadOfSebath";
    private final Map<Integer, RunicShopItem> availableItems;

    public CaveShop() {
        availableItems = new HashMap<>();
        int nextItemIndex = 0;
        ItemStack bow = RunicItemsAPI.generateItemFromTemplate("sanguine_longbow").generateItem();
        availableItems.put(nextItemIndex++, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                iconWithLore(bow, ARTIFACT_PRICE), runShopBuy(bow)));
        ItemStack mace = RunicItemsAPI.generateItemFromTemplate("crimson_maul").generateItem();
        availableItems.put(nextItemIndex++, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                iconWithLore(mace, ARTIFACT_PRICE), runShopBuy(mace)));
        ItemStack staff = RunicItemsAPI.generateItemFromTemplate("bloodmoon").generateItem();
        availableItems.put(nextItemIndex++, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                iconWithLore(staff, ARTIFACT_PRICE), runShopBuy(staff)));
        ItemStack sword = RunicItemsAPI.generateItemFromTemplate("scarlet_rapier").generateItem();
        availableItems.put(nextItemIndex++, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                iconWithLore(sword, ARTIFACT_PRICE), runShopBuy(sword)));
        ItemStack axe = RunicItemsAPI.generateItemFromTemplate("corruption").generateItem();
        availableItems.put(nextItemIndex, new RunicShopItem(ARTIFACT_PRICE, MYTHIC_CURRENCY,
                iconWithLore(axe, ARTIFACT_PRICE), runShopBuy(axe)));
        nextItemIndex = 9;
        nextItemIndex = loadRunicArmor(nextItemIndex,
                "sebaths-cave-archer-helmet",
                "sebaths-cave-archer-chest",
                "sebaths-cave-archer-leggings",
                "sebaths-cave-archer-boots");
        nextItemIndex = loadRunicArmor(nextItemIndex,
                "sebaths-cave-cleric-helmet",
                "sebaths-cave-cleric-chest",
                "sebaths-cave-cleric-leggings",
                "sebaths-cave-cleric-boots");
        nextItemIndex = loadRunicArmor(nextItemIndex,
                "sebaths-cave-mage-helmet",
                "sebaths-cave-mage-chest",
                "sebaths-cave-mage-leggings",
                "sebaths-cave-mage-boots");
        nextItemIndex = loadRunicArmor(nextItemIndex,
                "sebaths-cave-rogue-helmet",
                "sebaths-cave-rogue-chest",
                "sebaths-cave-rogue-leggings",
                "sebaths-cave-rogue-boots");
        loadRunicArmor(nextItemIndex,
                "sebaths-cave-warrior-helmet",
                "sebaths-cave-warrior-chest",
                "sebaths-cave-warrior-leggings",
                "sebaths-cave-warrior-boots");
        RunicCoreAPI.registerRunicItemShop(this);
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
            ItemStack itemStack = RunicItemsAPI.generateItemFromTemplate(s).generateItem();
            availableItems.put(nextItemIndex, new RunicShopItem(ARMOR_PRICE, MYTHIC_CURRENCY,
                    iconWithLore(itemStack, CaveShop.ARMOR_PRICE), runShopBuy(itemStack)));
            nextItemIndex += 9;
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
            meta.setDisplayName(ChatColor.YELLOW + "Sebath's Cave Dungeon Shop");
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
        return ChatColor.YELLOW + "Sebath's Cave Dungeon Shop";
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
                            price + " Head(s) of Sebath"
            );
            meta.setLore(lore);
            iconWithLore.setItemMeta(meta);
        }
        return iconWithLore;
    }
}
