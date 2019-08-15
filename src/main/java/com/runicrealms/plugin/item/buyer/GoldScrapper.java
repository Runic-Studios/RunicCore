package com.runicrealms.plugin.item.buyer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Item Menu to scrap items
 */
public class GoldScrapper {

    private static final int MAX_COMMON_GOLD = 7;
    private static final int MAX_UNCOMMON_GOLD = 15;
    private static final int MAX_RARE_GOLD = 30;
    private static final int MAX_EPIC_GOLD = 60;
    private static final int MAX_LEGENDARY_GOLD = 120;

    public GoldScrapper() {}

    public ItemGUI openMenu(Player pl) {

        // name the menu
        ItemGUI scrapperMenu = new ItemGUI();
        scrapperMenu.setName("&f&l" + pl.getName() + "'s &6&lArmor Scrapper");

        //set the visual items
        scrapperMenu.setOption(7, new ItemStack(Material.SLIME_BALL),
                "&aScrap Armor", "&7Scrap armor and receive &6gold&7!", 0, false);
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
                dropItemsInInventory(pl, scrapperMenu);
                event.setWillClose(true);
                event.setWillDestroy(true);
            }
        });

        return scrapperMenu;
    }

    // todo: prevent quest items, prevent equipping helmets if crafting screen is open
    /**
     * This method reads the items in the first seven slots of the menu,
     * removes them, and then decides how much gold to dish out.
     */
    private void scrapItems(Player pl, ItemGUI goldScrapperMenu) {

        Random rand = new Random();
        int goldToGive = 0;

        // loot through items
        for (int i = 0; i < 7; i++) {
            if (goldScrapperMenu.getItem(i) == null) continue;
            ItemStack itemStack = goldScrapperMenu.getItem(i);
            if (!itemStack.hasItemMeta()) continue;
            if (!Objects.requireNonNull(itemStack.getItemMeta()).hasLore()) continue;
            List<String> lore = itemStack.getItemMeta().getLore();

            if (lore != null) {
                if (lore.contains(ChatColor.GRAY + "Common")) {
                    goldToGive += rand.nextInt(MAX_COMMON_GOLD - 2) + 2; // 2-7
                } else if (lore.contains(ChatColor.GREEN + "Uncommon")) {
                    goldToGive += rand.nextInt(MAX_UNCOMMON_GOLD - MAX_COMMON_GOLD) + MAX_COMMON_GOLD; // 7-15
                } else if (lore.contains(ChatColor.AQUA + "Rare")) {
                    goldToGive += rand.nextInt(MAX_RARE_GOLD - MAX_UNCOMMON_GOLD) + MAX_UNCOMMON_GOLD; // 15-30
                } else if (lore.contains(ChatColor.LIGHT_PURPLE + "Epic")) {
                    goldToGive += rand.nextInt(MAX_EPIC_GOLD - MAX_RARE_GOLD) + MAX_RARE_GOLD; // 30-60
                } else if (lore.contains(ChatColor.GOLD + "Legendary")) {
                    goldToGive += rand.nextInt(MAX_LEGENDARY_GOLD - MAX_EPIC_GOLD) + MAX_EPIC_GOLD; // 60-120
                }
            }
        }

        if (goldToGive > 0) {
            giveGold(pl, goldToGive);
        } else {
            pl.sendMessage(ChatColor.GRAY + "Place armor inside the menu to scrap the items!");
        }
    }

    private void giveGold(Player pl, int goldToGive) {

        pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        pl.sendMessage(ChatColor.GREEN + "You received " + ChatColor.GOLD + goldToGive + ChatColor.GREEN + " gold for your items!");

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

    private void dropItemsInInventory(Player pl, ItemGUI goldScrapperMenu) {

        for (int i = 0; i < 7; i++) {
            if (goldScrapperMenu.getItem(i) == null) continue;
            ItemStack itemStack = goldScrapperMenu.getItem(i);
            if (pl.getInventory().firstEmpty() != -1) {
                pl.getInventory().addItem(itemStack);
            } else {
                pl.getWorld().dropItem(pl.getLocation(), itemStack);
            }
        }
    }
}
