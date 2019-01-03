package us.fortherealm.plugin.item;

import de.tr7zw.itemnbtapi.NBTItem;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.utilities.NumRounder;

import java.util.ArrayList;

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
        lore.add(ChatColor.YELLOW + "Artifact");
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
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Ancient Rune");

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
        lore.add(ChatColor.LIGHT_PURPLE + "Rune");
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // update lore, meta
        meta.setLore(lore);
        rune.setItemMeta(meta);
    }

    public static void generateItemLore(ItemStack item, ChatColor color, String dispName, String rarity, String type) {

        // grab our ItemMeta, ItemLore
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        meta.setDisplayName(color + dispName);

        lore.add("");

        int health = (int) AttributeUtil.getGenericDouble(item, "generic.maxHealth");
        lore.add(ChatColor.RED + "+ " + health + "❤");

        lore.add("");

        lore.add(color + rarity);
        lore.add(ChatColor.GRAY + type);

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
