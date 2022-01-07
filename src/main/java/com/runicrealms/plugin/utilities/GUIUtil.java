package com.runicrealms.plugin.utilities;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GUIUtil {

    public static ItemStack backButton() {
        ItemStack backButton = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = backButton.getItemMeta();
        if (meta == null) return backButton;
        meta.setDisplayName(ChatColor.RED + "Return");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Return to the previous menu"));
        backButton.setItemMeta(meta);
        return backButton;
    }

    public static ItemStack borderItem() {
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = borderItem.getItemMeta();
        if (meta == null) return borderItem;
        meta.setDisplayName(ChatColor.GRAY + "");
        borderItem.setItemMeta(meta);
        return borderItem;
    }

    public static ItemStack closeButton() {
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = closeButton.getItemMeta();
        if (meta == null) return closeButton;
        meta.setDisplayName(ChatColor.RED + "Close");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Close the menu"));
        closeButton.setItemMeta(meta);
        return closeButton;
    }

    // creates the visual menu w/ String
    public static ItemStack dispItem(Material material, ChatColor color, String displayName, String description) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + displayName);
        String[] desc = description.split("\n");
        ArrayList<String> lore = new ArrayList<>();
        for (String line : desc) {
            lore.add(ChatColor.GRAY + line);
        }
        meta.setLore(lore);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    // creates the visual menu w/ ArrayList
    public static ItemStack dispItem(Material material, ChatColor color, String displayName, ArrayList<String> desc) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + displayName);
        meta.setLore(desc);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    // creates the visual menu w/ String
    public static ItemStack dispItem(Material material, ChatColor color, String displayName, String description, int durab) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + displayName);
        String[] desc = description.split("\n");
        ArrayList<String> lore = new ArrayList<>();
        for (String line : desc) {
            lore.add(ChatColor.GRAY + line);
        }
        meta.setLore(lore);
        ((Damageable) meta).setDamage(durab);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack dispItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * This...
     *
     * @param material
     * @param name
     * @param lore
     * @param durability
     * @return
     */
    public static ItemStack dispItem(Material material, String name, String[] lore, Integer... durability) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> list = new ArrayList<String>();
        for (String line : lore) {
            list.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(list);
        if (durability.length > 0) {
            ((Damageable) meta).setDamage(durability[0]);
            meta.setUnbreakable(true);
        }
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack setLore(ItemStack item, String[] lore) {
        ItemMeta meta = item.getItemMeta();
        List<String> list = new ArrayList<String>();
        for (String line : lore) {
            list.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(list);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack setName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        return item;
    }

    public static void fillInventoryBorders(Inventory inventory) {
        ItemStack background = borderItem();
        int[] slots = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48,
                49, 50, 51, 52, 53};
        for (int slot : slots) {
            inventory.setItem(slot, background);
        }
    }
}
