package com.runicrealms.plugin.item.scrapper;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.enums.ItemTypeEnum;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.item.shops.RunicShop;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import org.bukkit.Bukkit;
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

    private static final int MAX_CRAFTED_GOLD = 8;
    private static final int MAX_COMMON_GOLD = 4;
    private static final int MAX_UNCOMMON_GOLD = 8;
    private static final int MAX_RARE_GOLD = 16;
    private static final int MAX_EPIC_GOLD = 32;
    private static final int MAX_LEGENDARY_GOLD = 128;

    private final HashMap<UUID, List<ItemStack>> storedItems;

    public ItemScrapper(Player pl) {
        setupShop(pl);
        storedItems = new HashMap<>();
        List<ItemStack> items = new ArrayList<>();
        storedItems.put(pl.getUniqueId(), items);
    }

    @Override
    public void setupShop(Player pl) {

        super.setupShop("&eItem Scrapper", false);
        ItemGUI scrapperMenu = getItemGUI();

        //set the visual items
        scrapperMenu.setOption(7, new ItemStack(Material.SLIME_BALL),
                "&aScrap Items", "&7Scrap items and receive &6gold&7!", 0, false);
        scrapperMenu.setOption(8, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0, false);

        // set the handler
        scrapperMenu.setHandler(event -> {

            // convert the items to gold
            if (event.getSlot() == 7) {
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                scrapItems(pl, scrapperMenu);
                event.setWillClose(true);
                event.setWillDestroy(true);

            // close editor
            } else if (event.getSlot() == 8) {
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
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
     * @param player to give gold to
     * @param goldScrapperMenu
     */
    private void scrapItems(Player player, ItemGUI goldScrapperMenu) {

        Random rand = new Random();
        int goldToGive = 0;

        // loot through items
        for (int i = 0; i < 7; i++) {
            if (goldScrapperMenu.getItem(i) == null) continue;
            ItemStack itemStack = goldScrapperMenu.getItem(i);
            if (!itemStack.hasItemMeta()) continue;
            if (!Objects.requireNonNull(itemStack.getItemMeta()).hasLore()) continue;
            List<String> lore = itemStack.getItemMeta().getLore();
            ItemTypeEnum itemTypeEnum = ItemTypeEnum.matchType(itemStack);
            if (itemTypeEnum == ItemTypeEnum.GEMSTONE) continue;

            if (lore != null) {
                if (lore.contains(ChatColor.DARK_GRAY + "Soulbound")) {
                    player.sendMessage(ChatColor.RED + "You cannot scrap soulbound items!");
                } else if (lore.contains(ChatColor.WHITE + "Crafted")) {
                    goldToGive += rand.nextInt(MAX_CRAFTED_GOLD - 1) + 1; // 1-8
                    storedItems.get(player.getUniqueId()).add(itemStack);
                } else if (lore.contains(ChatColor.GRAY + "Common")) {
                    goldToGive += rand.nextInt(MAX_COMMON_GOLD - 1) + 1; // 1-4
                    storedItems.get(player.getUniqueId()).add(itemStack);
                } else if (lore.contains(ChatColor.GREEN + "Uncommon")) {
                    goldToGive += rand.nextInt(MAX_UNCOMMON_GOLD - MAX_COMMON_GOLD) + MAX_COMMON_GOLD; // 4-8
                    storedItems.get(player.getUniqueId()).add(itemStack);
                } else if (lore.contains(ChatColor.AQUA + "Rare")) {
                    goldToGive += rand.nextInt(MAX_RARE_GOLD - MAX_UNCOMMON_GOLD) + MAX_UNCOMMON_GOLD; // 8-16
                    storedItems.get(player.getUniqueId()).add(itemStack);
                } else if (lore.contains(ChatColor.LIGHT_PURPLE + "Epic")) {
                    goldToGive += rand.nextInt(MAX_EPIC_GOLD - MAX_RARE_GOLD) + MAX_RARE_GOLD; // 16-32
                    storedItems.get(player.getUniqueId()).add(itemStack);
                } else if (lore.contains(ChatColor.GOLD + "Legendary")) {
                    goldToGive += rand.nextInt(MAX_LEGENDARY_GOLD - MAX_EPIC_GOLD) + MAX_EPIC_GOLD; // 32-120
                    storedItems.get(player.getUniqueId()).add(itemStack);
                }
            }
        }

        if (goldToGive > 0) {
            giveGold(player, goldToGive);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.GRAY + "Place an armor piece or weapon inside the menu to scrap it!");
        }
    }

    /**
     * This...
     *
     * @param pl
     * @param goldToGive
     */
    private void giveGold(Player pl, int goldToGive) {

        pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        pl.sendMessage(ChatColor.GREEN + "You received " + ChatColor.GOLD + goldToGive + ChatColor.GREEN + " gold for your item(s)!");

        int numOfFullStacks = goldToGive / 64;
        int remainder = goldToGive % 64;

        // check that the player has an open inventory space
        // this method prevents items from stacking if the player crafts 5
        for (int i = 0; i < numOfFullStacks; i++) {
            if (pl.getInventory().firstEmpty() != -1) {
                pl.getInventory().addItem(CurrencyUtil.goldCoin(64));
            } else {
                pl.getWorld().dropItem(pl.getLocation(), CurrencyUtil.goldCoin(64));
            }
        }

        // give remaining stacks
        if (pl.getInventory().firstEmpty() != -1) {
            pl.getInventory().addItem(CurrencyUtil.goldCoin(remainder));
        } else {
            pl.getWorld().dropItem(pl.getLocation(), CurrencyUtil.goldCoin(remainder));
        }

        // update player inventory
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), pl::updateInventory, 1);
    }

    public HashMap<UUID, List<ItemStack>> getStoredItems() {
        return storedItems;
    }
}
