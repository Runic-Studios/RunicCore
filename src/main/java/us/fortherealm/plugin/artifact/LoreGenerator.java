package us.fortherealm.plugin.artifact;

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
        double roundedSpeed = round(24+speed);
        lore.add(ChatColor.RED + "Att Speed: " + roundedSpeed);
        lore.add(ChatColor.RED + "DMG: " + (int) min + "-" + (int) max);
    }

    // rounds to 2 decimal places
    private static double round(double value) {
        int scale = (int) Math.pow(10, 2);
        return (double) Math.round(value * scale) / scale;
    }
}
