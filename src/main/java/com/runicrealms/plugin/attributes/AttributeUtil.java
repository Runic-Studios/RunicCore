package com.runicrealms.plugin.attributes;

import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTListCompound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class AttributeUtil {

    // takes an ItemStack, returns a new ItemStack with applied attributes
    public static ItemStack addCustomStat(ItemStack item, String statName, double amount) {
        NBTItem nbti = new NBTItem(item);
        nbti.setDouble(statName, amount);
        item = nbti.getItem();
        return item;
    }

    // overloaded method to add a stat by String
    public static ItemStack addCustomStat(ItemStack item, String statName, String value) {
        NBTItem nbti = new NBTItem(item);
        nbti.setString(statName, value);
        item = nbti.getItem();
        return item;
    }

    // takes an ItemStack, returns a new ItemStack with applied attributes
    public static ItemStack addGenericStat(ItemStack item, String statName, double amt, String slot) {
        ItemStack artifact = item;
        NBTItem nbti = new NBTItem(artifact);
        NBTCompoundList attributes = nbti.getCompoundList("AttributeModifiers");
        NBTListCompound attribute = attributes.addCompound();
        attribute.setDouble("Amount", amt);
        attribute.setString("AttributeName", statName);
        attribute.setString("Name", statName);
        attribute.setString("Slot", slot);
        attribute.setInteger("Operation", 0);
        attribute.setInteger("UUIDLeast", 1);
        int maxUUID;
        switch (slot) {
            case "head":
                maxUUID = 3;
                break;
            case "chest":
                maxUUID = 5;
                break;
            case "legs":
                maxUUID = 7;
                break;
            case "feet":
                maxUUID = 9;
                break;
            case "offhand":
                maxUUID = 11;
                break;
            default:
                maxUUID = 13;
                break;
        }
        attribute.setInteger("UUIDMost", maxUUID);
        artifact = nbti.getItem();
        return artifact;
    }

    // overloaded method to specify the UUID of the attribute
    public static ItemStack addGenericStat(ItemStack item, String statName, double amt, String slot, int uuid) {
        ItemStack artifact = item;
        NBTItem nbti = new NBTItem(artifact);
        NBTCompoundList attributes = nbti.getCompoundList("AttributeModifiers");
        NBTListCompound attribute = attributes.addCompound();
        attribute.setDouble("Amount", amt);
        attribute.setString("AttributeName", statName);
        attribute.setString("Name", statName);
        attribute.setString("Slot", slot);
        attribute.setInteger("Operation", 0);
        attribute.setInteger("UUIDLeast", 1);
        attribute.setInteger("UUIDMost", uuid);
        artifact = nbti.getItem();
        return artifact;
    }

    // takes an ItemStack, returns a new ItemStack with applied spell
    public static ItemStack addSpell(ItemStack item, String spellSlot, String spell) {
        NBTItem nbti = new NBTItem(item);
        if (nbti.hasKey(spellSlot)) {
            nbti.removeKey(spellSlot);
        }
        nbti.setString(spellSlot, spell);
        item = nbti.getItem();
        return item;
    }

    // searches for a custom stat on an item
    public static double getCustomDouble(ItemStack item, String statName) {
        if (item.getType() == Material.AIR) return 0;
        NBTItem nbti = new NBTItem(item);
        if (nbti.hasKey(statName)) {
            return nbti.getDouble(statName);
        } else {
            return 0;
        }
    }

    // above method but with Strings
    public static String getCustomString(ItemStack item, String statName) {
        if (item.getType() == Material.AIR) return "";
        NBTItem nbti = new NBTItem(item);
        if (nbti.hasKey(statName)) {
            return nbti.getString(statName);
        } else {
            return "";
        }
    }

    // searches for a given stat on an item
    public static double getGenericDouble(ItemStack item, String name) {
        if (item.getType() == Material.AIR) return 0;
        double amount = 0;
        NBTItem nbti = new NBTItem(item);
        NBTCompoundList list = nbti.getCompoundList("Attributes");
        for (NBTListCompound lc : list) {
            if (lc.getString("Name").equals(name)) {
                amount = lc.getDouble("Amount");
            }
        }
        return amount;
    }

    // finds a spell
    public static String getSpell(ItemStack item, String spellSlot) {
        NBTItem nbti = new NBTItem(item);
        return nbti.getString(spellSlot);
    }
}
