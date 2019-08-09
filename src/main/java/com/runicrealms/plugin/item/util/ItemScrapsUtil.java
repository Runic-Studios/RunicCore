package com.runicrealms.plugin.item.util;

import com.runicrealms.plugin.item.LoreGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemScrapsUtil {

    /**
     * giveScrap gives an item that can be used to repair armor.
     * @param pl player who will receive scrap
     * @param tier correspends to the tier of scrap to be given
     */
    public static void giveScrap(Player pl, int tier) {

        ItemStack scrap = new ItemStack(Material.AIR);
        ItemMeta meta = scrap.getItemMeta();
        if (meta == null) return;

        switch(tier) {
            // legendary scrap
            case 5:
                break;
            // epic scrap
            case 4:
                scrap.setType(Material.PURPLE_DYE);
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Epic Armor Scrap");
                break;
            // rare scrap
            case 3:
                scrap.setType(Material.CYAN_DYE);
                meta.setDisplayName(ChatColor.AQUA + "Rare Armor Scrap");
                break;
            // uncommon scrap
            case 2:
                scrap.setType(Material.LIME_DYE);
                meta.setDisplayName(ChatColor.GREEN + "Uncommon Armor Scrap");
                break;
            // common scrap
            case 1:
            default:
                scrap.setType(Material.GRAY_DYE);
                meta.setDisplayName(ChatColor.GRAY + "Common Armor Scrap");
                break;
        }

        scrap.setItemMeta(meta);
        LoreGenerator.generateItemLore(scrap, ChatColor.GRAY, "Common Item Scrap", "I can use this to repair my armor and tools!");

        // give item
        if (pl.getInventory().firstEmpty() != -1) {
            int firstEmpty = pl.getInventory().firstEmpty();
            pl.getInventory().setItem(firstEmpty, scrap);
        } else {
            pl.getWorld().dropItem(pl.getLocation(), scrap);
        }
    }
}
