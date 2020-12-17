package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SpellEditorGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;

    public SpellEditorGUI(Player player) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&d&lSpell Editor"));
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
        this.inventory.setItem(0, GUIUtil.backButton());
        this.inventory.setItem(31, ancientRune());
        this.inventory.setItem(10, spellButtonHotbarOne());
        this.inventory.setItem(16, spellButtonHotbarOne());
        this.inventory.setItem(46, spellButtonHotbarOne());
        this.inventory.setItem(52, spellButtonHotbarOne());
    }

    private ItemStack ancientRune() {
        ItemStack skillTreeButton = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        ItemMeta meta = skillTreeButton.getItemMeta();
        if (meta == null) return skillTreeButton;
        meta.setDisplayName(ChatColor.GREEN + "Your Spell Setup:");
        String lore = "&a[1] Spell 'Hotbar 1'\n&[L] Spell 'Left-click'\n&a[R] Spell 'Right-click'\n&a[F] Spell 'Swap-hands";
        meta.setLore(ChatUtils.formattedText(lore));
        skillTreeButton.setItemMeta(meta);
        return skillTreeButton;
    }

    public static ItemStack spellButtonHotbarOne() {
        ItemStack spellEditorButton = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = spellEditorButton.getItemMeta();
        if (meta == null) return spellEditorButton;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Open Spell Editor");
        meta.setLore(ChatUtils.formattedText(ChatColor.GRAY + "Configure your active spells! " +
                "Set spells to be executed by different key combos!"));
        spellEditorButton.setItemMeta(meta);
        return spellEditorButton;
    }
}