package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SpellGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final String spellSlot;

    public SpellGUI(Player player, String spellSlot) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&a&lAvailable Spells"));
        this.player = player;
        this.spellSlot = spellSlot;
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

    public String getSpellSlot() {
        return  this.spellSlot;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        this.inventory.setItem(0, GUIUtil.backButton());
        int i = 9;
        grabUnlockedSpellsFromTree(1, i);
        grabUnlockedSpellsFromTree(2, i);
        grabUnlockedSpellsFromTree(3, i);
    }

    /**
     *
     * @param treePosition
     * @param index
     */
    private void grabUnlockedSpellsFromTree(int treePosition, int index) {
        if (RunicCoreAPI.getSkillTree(player, treePosition) == null) return;
        for (Perk perk : RunicCoreAPI.getSkillTree(player, treePosition).getPerks()) {
            if (perk.getCurrentlyAllocatedPoints() < perk.getCost()) continue;
            if (!(perk instanceof PerkSpell)) continue;
            if (RunicCoreAPI.getSpell(((PerkSpell) perk).getSpellName()).isPassive()) continue;
            this.getInventory().setItem(index, SkillTreeGUI.buildPerkItem(perk, false, ChatColor.LIGHT_PURPLE + "» Click to activate"));
            index++;
        }
    }
}