package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;

public class RuneGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;

    public RuneGUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 27, ColorUtil.format("&dAncient Rune"));
        this.player = player;
        openMenu();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        this.inventory.setItem(11, skillTreeButton(ClassEnum.MAGE));
        this.inventory.setItem(13, spellEditorButton());
        this.inventory.setItem(15, closeButton());
    }

    public static ItemStack skillTreeButton(ClassEnum classEnum) {
        ItemStack skillTreeButton = new ItemStack(Material.PAPER);
        ItemMeta meta = skillTreeButton.getItemMeta();
        if (meta == null) return skillTreeButton;
        meta.setDisplayName(ChatColor.GREEN + "Open Skill Trees");
        String lore = ChatColor.GRAY + "Open the skill trees for the " +
                ChatColor.GREEN + classEnum.getName() +
                ChatColor.GRAY + " class! Earn skill points by leveling-up " +
                "and spend them on unique and powerful perks!";
        String[] loreArr = ChatPaginator.wordWrap(lore, 25);
        meta.setLore(Arrays.asList(loreArr));
        skillTreeButton.setItemMeta(meta);
        return skillTreeButton;
    }

    // todo: read their currently set spells
    public static ItemStack spellEditorButton() {
        ItemStack spellEditorButton = new ItemStack(Material.NETHER_WART);
        return spellEditorButton;
    }

    private static ItemStack closeButton() {
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = backButton.getItemMeta();
        if (meta == null) return backButton;
        meta.setDisplayName(ChatColor.RED + "Close");
        meta.setLore(Collections.singletonList(ChatColor.GRAY + "Close the Ancient Rune menu"));
        backButton.setItemMeta(meta);
        return backButton;
    }
}