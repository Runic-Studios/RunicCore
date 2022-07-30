package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

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
        this.inventory.setItem(11, skillTreeButton());
        this.inventory.setItem(13, spellEditorButton());
        this.inventory.setItem(15, closeButton());
    }

    public ItemStack skillTreeButton() {
        ItemStack skillTreeButton = new ItemStack(Material.PAPER);
        ItemMeta meta = skillTreeButton.getItemMeta();
        if (meta == null) return skillTreeButton;
        meta.setDisplayName(ChatColor.GREEN + "Open Skill Trees");
        String lore = ChatColor.GRAY + "Open the skill trees for the " +
                ChatColor.GREEN + RunicCoreAPI.getPlayerClass(player) +
                ChatColor.GRAY + " class! Earn skill points by leveling-up " +
                "and spend them on unique and powerful perks!";
        meta.setLore(ChatUtils.formattedText(lore));
        skillTreeButton.setItemMeta(meta);
        return skillTreeButton;
    }

    public static ItemStack spellEditorButton() {
        ItemStack spellEditorButton = new ItemStack(Material.NETHER_WART);
        ItemMeta meta = spellEditorButton.getItemMeta();
        if (meta == null) return spellEditorButton;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Open Spell Editor");
        meta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "Configure your active spells! " +
                "Set spells to be executed by different key combos!"));
        spellEditorButton.setItemMeta(meta);
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