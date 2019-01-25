package us.fortherealm.plugin.attributes;

import de.tr7zw.itemnbtapi.NBTItem;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

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
        NBTList attributes = nbti.getList("AttributeModifiers", NBTType.NBTTagCompound);
        NBTListCompound attribute = attributes.addCompound();
        attribute.setDouble("Amount", amt);
        attribute.setString("AttributeName", statName);
        attribute.setString("Name", statName);
        attribute.setString("Slot", slot);
        attribute.setInteger("Operation", 0);
        attribute.setInteger("UUIDLeast", 1);
        int maxUUID = 2;
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
            default:
                maxUUID = 11;
                break;
        }
        attribute.setInteger("UUIDMost", maxUUID);
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
        NBTItem nbti = new NBTItem(item);
        if (nbti.hasKey(statName)) {
            return nbti.getDouble(statName);
        } else {
            return 0;
        }
    }

    // above method but with Strings
    public static String getCustomString(ItemStack item, String statName) {
        NBTItem nbti = new NBTItem(item);
        if (nbti.hasKey(statName)) {
            return nbti.getString(statName);
        } else {
            return "";
        }
    }

    // searches for a given stat on an item
    public static double getGenericDouble(ItemStack item, String name) {
        double amount = 0;
        NBTItem nbti = new NBTItem(item);
        NBTList list = nbti.getList("AttributeModifiers", NBTType.NBTTagCompound);
        for (int i = 0; i < list.size(); i++) {
            NBTListCompound lc = list.getCompound(i);
            if (lc.getString("Name").equals(name)) {
                amount = lc.getDouble("Amount");
            }
        }
        if (amount != 0) {
            return amount;
        } else {
            return 0;
        }
    }

    // finds a spell
    public static String getSpell(ItemStack item, String spellSlot) {
        NBTItem nbti = new NBTItem(item);
        return nbti.getString(spellSlot);
    }
}
