package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.classes.SubClass;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SubClassGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final int slot;

    /**
     * @param player who opened the gui
     * @param slot   of the character
     */
    public SubClassGUI(Player player, int slot) {
        this.inventory = Bukkit.createInventory(this, 27, ColorUtil.format("&aChoose a sub-class!"));
        this.player = player;
        this.slot = slot;
        setupMenu();
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
    private void setupMenu() {
        this.inventory.clear();
        this.inventory.setItem(0, GUIUtil.backButton());
        this.inventory.setItem(11, subClassItem(SkillTreePosition.FIRST, slot));
        this.inventory.setItem(13, subClassItem(SkillTreePosition.SECOND, slot));
        this.inventory.setItem(15, subClassItem(SkillTreePosition.THIRD, slot));
    }

    /**
     * Adds an ItemStack with some type of glazed terracotta to represent a subclass.
     *
     * @param position which subclass
     * @return an ItemStack icon
     */
    private ItemStack subClassItem(SkillTreePosition position, int slot) {
        SubClass subClass = SubClass.determineSubClass(player.getUniqueId(), position, slot);
        String displayName = subClass.getName();
        ItemStack subClassItem = subClass.getItemStack();
        ItemMeta meta = subClassItem.getItemMeta();
        if (meta == null) return subClassItem;
        meta.setDisplayName(ChatColor.GREEN + displayName);
        String lore = ChatColor.GRAY + "Open the skill tree for the " +
                ChatColor.GREEN + displayName +
                ChatColor.GRAY + " class! " + subClass.getDescription();
        meta.setLore(ChatUtils.formattedText(lore));
        subClassItem.setItemMeta(meta);
        return subClassItem;
    }
}