package us.fortherealm.plugin.artifact;

import de.tr7zw.itemnbtapi.NBTItem;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class LoreGenerator {

    public static void generateArtifactLore(ItemStack artifact, ChatColor color, String itemName, String className, int durability) {

        // grab our ItemMeta, ItemLore
        ItemMeta meta = artifact.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        meta.setDisplayName(color + itemName);

        // grab our NBT attributes wrapper
        NBTItem nbti = new NBTItem(artifact);
        NBTList attributes = nbti.getList("AttributeModifiers", NBTType.NBTTagCompound);

        // spell display
        String prim = nbti.getString("primarySpell");
        String sec = nbti.getString("secondarySpell");
        lore.add("");
        lore.add(ChatColor.GRAY + "Spells:");
        if (prim != null) {
            if (artifact.getType() == Material.BOW) {
                lore.add(ChatColor.WHITE + "Left click: " + ChatColor.GREEN + prim);
            } else {
                lore.add(ChatColor.WHITE + "Shift + left: " + ChatColor.GREEN + prim);
            }
        } else {
            lore.add(ChatColor.RED + "Primary: NULL");
        }
        if (sec != null) {
            if (artifact.getType() == Material.BOW) {
                lore.add(ChatColor.WHITE + "Shift + right: " + ChatColor.GREEN + sec);
            } else {
                lore.add(ChatColor.WHITE + "Right click: " + ChatColor.GREEN + sec);
            }
        } else {
            lore.add(ChatColor.RED + "Secondary: NULL");
        }

        // stat display
        lore.add("");
        lore.add(ChatColor.GRAY + "Stats:");
        switch (className) {
            case "Archer":
                for (int i = 0; i < attributes.size(); i++) {
                    NBTListCompound stat = attributes.getCompound(i);
                    String name = stat.getString("Name");
                    if (name.equals("custom.bowSpeed")) {
                        lore.add(ChatColor.RED + "Att Speed: " + stat.getDouble("Amount"));
                    } else if (name.equals("custom.bowDamage")) {
                        lore.add(ChatColor.RED + "DMG: " + (int) (stat.getDouble("Amount")+1)/2);
                    }
                }
                break;
            case "Cleric":
                fillLore(lore, attributes);
                break;
            case "Mage":
                for (int i = 0; i < attributes.size(); i++) {
                    NBTListCompound stat = attributes.getCompound(i);
                    String name = stat.getString("Name");
                    if (name.equals("generic.attackSpeed")) {
                        lore.add(ChatColor.RED + "Att Speed: " + (24+stat.getDouble("Amount")));
                    } else if (name.equals("custom.staffDamage")) {
                        lore.add(ChatColor.RED + "DMG: " + (int) (stat.getDouble("Amount")+1)/2);
                    }
                }
                break;
            case "Rogue":
                fillLore(lore, attributes);
                break;
            case "Warrior":
                fillLore(lore, attributes);
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

    private static void fillLore(ArrayList<String> lore, NBTList attributes) {
        for (int i = 0; i < attributes.size(); i++) {
            NBTListCompound stat = attributes.getCompound(i);
            String name = stat.getString("Name");
            if (name.equals("generic.attackSpeed")) {
                lore.add(ChatColor.RED + "Att Speed: " + (24+stat.getDouble("Amount")));
            } else if (name.equals("generic.attackDamage")) {
                lore.add(ChatColor.RED + "DMG: " + (int) (stat.getDouble("Amount")+1)/2);
            }
        }
    }
}
