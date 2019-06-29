package com.runicrealms.plugin.professions.gathering;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Objects;

public class GatheringUtil {

    public static void generateToolLore(ItemStack item, int tier) {

        int maxDurab = 0;
        String successRate = "";
        String tierStr = "";
        switch (tier) {
            case 5:
                maxDurab = 800;
                successRate = "&a75";
                tierStr = "&6Legendary";
                break;
            case 4:
                maxDurab = 400;
                successRate = "&a62.5";
                tierStr = "&dEpic";
                break;
            case 3:
                maxDurab = 200;
                successRate = "&e50";
                tierStr = "&bRare";
                break;
            case 2:
                maxDurab = 100;
                successRate = "&c37.5";
                tierStr = "&aUncommon";
                break;
            case 1:
                maxDurab = 50;
                successRate = "&c25";
                tierStr = "&7Common";
                break;
        }

        // grab our material, ItemMeta, ItemLore
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();

        // build the lore
        String desc = "\n&7Durability: &f" + (int) AttributeUtil.getCustomDouble(item, "durability")
                + "&7/" + maxDurab + "\n&7Success Rate: " + successRate + "%\n\n" + tierStr;
        for (String s : desc.split("\n")) {
            lore.add(ColorUtil.format(s));
        }

        // set other flags
        Objects.requireNonNull(meta).setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // update lore, meta
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static ItemStack getGatheringTool(Material type, int tier) {

        // create gathering tool
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();

        String weapType = "";
        int durability;
        String tierName;

        switch (type) {
            case FISHING_ROD:
                weapType = "Fishing Rod";
                break;
            case IRON_AXE:
                weapType = "Woodcutting Axe";
                break;
            case IRON_HOE:
                weapType = "Farming Hoe";
            break;
            case IRON_PICKAXE:
                weapType = "Mining Pick";
                break;
        }

        switch (tier) {
            case 5:
                tierName = "&6Artisan";
                durability = 800;
                break;
            case 4:
                tierName = "&dMaster";
                durability = 400;
                break;
            case 3:
                tierName = "&bRefined";
                durability = 200;
                break;
            case 2:
                tierName = "&aAdept";
                durability = 100;
                break;
            case 1:
            default:
                tierName = "&7Basic";
                durability = 50;
                break;
        }

        // set the name
        Objects.requireNonNull(meta).setDisplayName(ColorUtil.format(tierName + " " + weapType));

        // complete this stuff first
        ((Damageable) meta).setDamage(tier);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);

        // must update durability stat AFTER setting the item meta
        item = AttributeUtil.addCustomStat(item, "durability", durability);

        // generate the lore
        GatheringUtil.generateToolLore(item, tier);

        return item;
    }
}
