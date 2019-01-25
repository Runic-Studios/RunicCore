package us.fortherealm.plugin.item;

import de.tr7zw.itemnbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.enums.ItemTypeEnum;
import us.fortherealm.plugin.utilities.NumRounder;

import java.util.ArrayList;
import java.util.Collections;

public class LoreGenerator {

    public static void generateArtifactLore(ItemStack artifact, ChatColor color, String itemName, String className, int durability) {

        // grab our ItemMeta, ItemLore
        ItemMeta meta = artifact.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        meta.setDisplayName(color + itemName);

        // grab our NBT attributes wrapper
        NBTItem nbti = new NBTItem(artifact);

        // spell display
        String prim = nbti.getString("primarySpell");
        String sec = nbti.getString("secondarySpell");
        lore.add("");
        lore.add(ChatColor.GRAY + "Spells:");
        if (prim != null) {
            if (artifact.getType() == Material.BOW) {
                lore.add(ChatColor.WHITE + "Left Click: " + ChatColor.GREEN + prim);
            } else {
                lore.add(ChatColor.WHITE + "Shift + Left: " + ChatColor.GREEN + prim);
            }
        } else {
            lore.add(ChatColor.RED + "Primary: NULL");
        }
        if (sec != null) {
            if (artifact.getType() == Material.BOW) {
                lore.add(ChatColor.WHITE + "Shift + Right: " + ChatColor.GREEN + sec);
            } else {
                lore.add(ChatColor.WHITE + "Right Click: " + ChatColor.GREEN + sec);
            }
        } else {
            lore.add(ChatColor.RED + "Secondary: NULL");
        }

        // stat display
        lore.add("");
        lore.add(ChatColor.GRAY + "Stats:");
        switch (className) {
            case "Archer":
                fillLore(lore, artifact, "Archer");
                break;
            case "Cleric":
                fillLore(lore, artifact, "Cleric");
                break;
            case "Mage":
                fillLore(lore, artifact, "Mage");
                break;
            case "Rogue":
                fillLore(lore, artifact, "Rogue");
                break;
            case "Warrior":
                fillLore(lore, artifact, "Warrior");
                break;
        }

        lore.add("");
        lore.add(ChatColor.WHITE + "Click " + ChatColor.GRAY + "this item to open the editor");
        lore.add("");
        lore.add(ChatColor.GOLD + "Artifact");
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // update lore, meta
        meta.setLore(lore);
        ((Damageable) meta).setDamage(durability);
        artifact.setItemMeta(meta);
    }

    public static void generateRuneLore(ItemStack rune) {

        // grab our ItemMeta, ItemLore
        ItemMeta meta = rune.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        meta.setDisplayName(ChatColor.GOLD + "Ancient Rune");

        // grab our NBT attributes wrapper
        NBTItem nbti = new NBTItem(rune);

        // spell display
        String prim = nbti.getString("primarySpell");
        String sec = nbti.getString("secondarySpell");
        lore.add("");
        lore.add(ChatColor.GRAY + "Spells:");
        if (prim != null) {
            lore.add(ChatColor.WHITE + "Left Click: " + ChatColor.GREEN + prim);
        } else {
            lore.add(ChatColor.RED + "Primary: NULL");
        }
        if (sec != null) {
            lore.add(ChatColor.WHITE + "Right Click: " + ChatColor.GREEN + sec);
        } else {
            lore.add(ChatColor.RED + "Secondary: NULL");
        }

        lore.add("");
        lore.add(ChatColor.WHITE + "Click " + ChatColor.GRAY + "this item to open the editor");
        lore.add("");
        lore.add(ChatColor.GOLD + "Rune");
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // update lore, meta
        meta.setLore(lore);
        rune.setItemMeta(meta);
    }

    public static void generateItemLore(ItemStack item, ChatColor dispColor, String dispName, String extra) {

        // grab our material, ItemMeta, ItemLore
        ItemTypeEnum itemType = ItemTypeEnum.matchType(item);
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        meta.setDisplayName(dispColor + dispName);

        int socketCount = (int) AttributeUtil.getCustomDouble(item, "custom.socketCount");
        int currentSockets = (int) AttributeUtil.getCustomDouble(item, "custom.currentSockets");
        if (socketCount != 0) {
            lore.add(ChatColor.GRAY + "[" + currentSockets + "/" + socketCount + "] Gems");
        }

        lore.add("");

        // for armor/items
        int health = (int) AttributeUtil.getGenericDouble(item, "generic.maxHealth");
        if (health != 0) {
            lore.add(ChatColor.RED + "+ " + health + "❤");
        }

        // for gemstones/custom boosts
        int customHealth = (int) AttributeUtil.getCustomDouble(item, "custom.maxHealth");
        double customAttSpeed = AttributeUtil.getCustomDouble(item, "custom.attackSpeed");
        int manaBoost = (int) AttributeUtil.getCustomDouble(item, "custom.manaBoost");
        if (customHealth != 0) {
            lore.add(ChatColor.RED + "+ " + customHealth + "❤");
        } else if (customAttSpeed != 0) {
            double roundedSpeed = NumRounder.round(customAttSpeed);
            lore.add(ChatColor.RED + "+ " + roundedSpeed + " Att Speed");
        } else if (manaBoost != 0) {
            lore.add(ChatColor.DARK_AQUA + "+ " + manaBoost + "✸");
        }

        lore.add("");

        // add rarity
        if (dispColor == ChatColor.WHITE) {
            lore.add(ChatColor.WHITE + "Crafted");
        } else if (dispColor == ChatColor.GRAY) {
            lore.add(ChatColor.GRAY + "Common");
        } else if (dispColor == ChatColor.GREEN) {
            lore.add(ChatColor.GREEN + "Uncommon");
        } else if (dispColor == ChatColor.AQUA) {
            lore.add(ChatColor.AQUA + "Rare");
        } else if (dispColor == ChatColor.LIGHT_PURPLE) {
            lore.add(ChatColor.LIGHT_PURPLE + "Epic");
        } else if (dispColor == ChatColor.GOLD) {
            lore.add(ChatColor.GOLD + "Legendary");
        }

        // add type of item lore
        String type;
        switch (itemType) {
            case CLOTH:
                type = "Cloth";
                break;
            case LEATHER:
                type = "Leather";
                break;
            case MAIL:
                type = "Mail";
                break;
            case PLATE:
                type = "Plate";
                break;
            case CRYSTAL:
                type = "Crystal";
                break;
            case GEMSTONE:
                type = "Gemstone";
                break;
            default:
                type = "Something went wrong";
                break;
        }
        lore.add(ChatColor.GRAY + type);

        // add additional lore if necessary
        if (!extra.equals("")) {
            String[] extraLore = extra.split("\n");
            Collections.addAll(lore, extraLore);
        }

        // set other flags
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // update lore, meta
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    // creates lore based on attributes
    private static void fillLore(ArrayList<String> lore, ItemStack item, String className) {
        double min = AttributeUtil.getCustomDouble(item, "custom.minDamage");
        double max = AttributeUtil.getCustomDouble(item, "custom.maxDamage");
        double speed;
        if (className.equals("Archer")) {
            speed = AttributeUtil.getCustomDouble(item, "custom.bowSpeed");
        } else {
            speed = AttributeUtil.getGenericDouble(item, "generic.attackSpeed");
        }
        double roundedSpeed = NumRounder.round(24+speed);
        lore.add(ChatColor.RED + "Att Speed: " + roundedSpeed);
        lore.add(ChatColor.RED + "DMG: " + (int) min + "-" + (int) max);
    }
}
