package us.fortherealm.plugin.rune;

import de.tr7zw.itemnbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class RuneLoreGenerator {

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
            lore.add(ChatColor.WHITE + "Left click: " + ChatColor.GREEN + prim);
        } else {
            lore.add(ChatColor.RED + "Primary: NULL");
        }
        if (sec != null) {
            lore.add(ChatColor.WHITE + "Right click: " + ChatColor.GREEN + sec);
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
}
