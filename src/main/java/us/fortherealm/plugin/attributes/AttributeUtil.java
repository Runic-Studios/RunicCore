package us.fortherealm.plugin.attributes;

import de.tr7zw.itemnbtapi.NBTItem;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.inventory.ItemStack;

public class AttributeUtil {

    // takes an ItemStack, returns a new ItemStack with applied attributes
    public static ItemStack addStat(ItemStack item, String modifier, double amount) {
        ItemStack artifact = item;
        NBTItem nbti = new NBTItem(artifact);
        NBTList attributes = nbti.getList("AttributeModifiers", NBTType.NBTTagCompound);
        NBTListCompound attribute = attributes.addCompound();
        attribute.setDouble("Amount", amount);
        attribute.setString("AttributeName", modifier);
        attribute.setString("Name", modifier);
        attribute.setInteger("Operation", 0);
        attribute.setInteger("UUIDLeast", 59764);
        attribute.setInteger("UUIDMost", 31483);
        artifact = nbti.getItem();
        return artifact;
    }

    // takes an ItemStack, returns a new ItemStack with applied attributes
    public static ItemStack addSpell(ItemStack item, String spellSlot, String spell) {
        ItemStack artifact = item;
        NBTItem nbti = new NBTItem(artifact);
        nbti.setString(spellSlot, spell);
        artifact = nbti.getItem();
        return artifact;
    }

    // searches for a given stat on an item
    public static double getValue(ItemStack item, String name) {
        double amount = 0;
        NBTItem nbti = new NBTItem(item);
        NBTList list = nbti.getList("AttributeModifiers", NBTType.NBTTagCompound);
        for (int i = 0; i < list.size(); i++) {
            NBTListCompound lc = list.getCompound(i);
            if (lc.getString("Name").equals(name)) {
                amount = lc.getDouble("Amount");
            }
        }
        return amount;
    }
}
