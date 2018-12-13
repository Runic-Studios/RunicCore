package us.fortherealm.plugin.attributes;

import de.tr7zw.itemnbtapi.NBTItem;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.inventory.ItemStack;

public class AttributeUtil {

    // takes an ItemStack, returns a new ItemStack with applied attributes
    public static ItemStack addStats(ItemStack item, String modifier, double amount) {
        ItemStack artifact = item;
        NBTItem nbti = new NBTItem(artifact);
        NBTList attribute = nbti.getList("AttributeModifiers", NBTType.NBTTagCompound);
        NBTListCompound mod1 = attribute.addCompound();
        mod1.setDouble("Amount", amount);
        mod1.setString("AttributeName", modifier);
        mod1.setString("Name", modifier);
        mod1.setInteger("Operation", 0);
        mod1.setInteger("UUIDLeast", 59764);
        mod1.setInteger("UUIDMost", 31483);
        artifact = nbti.getItem();
        return artifact;
    }
}
