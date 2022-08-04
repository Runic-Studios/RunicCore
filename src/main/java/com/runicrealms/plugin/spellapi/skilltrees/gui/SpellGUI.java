package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.redis.RedisField;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.spellapi.skilltrees.util.*;
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
        return this.spellSlot;
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        this.inventory.setItem(0, GUIUtil.backButton());
        this.inventory.setItem(9, SkillTreeGUI.buildPerkItem(determineDefaultSpellPerk(),
                false, ChatColor.LIGHT_PURPLE + "» Click to activate"));
        int i = 10;
        i = grabUnlockedSpellsFromTree(1, i);
        i = grabUnlockedSpellsFromTree(2, i);
        grabUnlockedSpellsFromTree(3, i);
    }

    /**
     * Returns a dummy 'perk' that is used to represent the default spell for each class.
     *
     * @return a perk that can be used to build an itemstack
     */
    private Perk determineDefaultSpellPerk() {
        int slot = RunicCoreAPI.getCharacterSlot(player.getUniqueId());
        switch (RunicCoreAPI.getRedisCharacterValue(player.getUniqueId(), RedisField.CLASS_TYPE.getField(), slot)) {
            case "Archer":
                return ArcherTreeUtil.DEFAULT_ARCHER_SPELL_PERK;
            case "Cleric":
                return ClericTreeUtil.DEFAULT_CLERIC_SPELL_PERK;
            case "Mage":
                return MageTreeUtil.DEFAULT_MAGE_SPELL_PERK;
            case "Rogue":
                return RogueTreeUtil.DEFAULT_ROGUE_SPELL_PERK;
            case "Warrior":
                return WarriorTreeUtil.DEFAULT_WARRIOR_SPELL_PERK;
            default:
                throw new IllegalStateException("Unexpected value: getting default spell perk. Check SpellGUI.java in RunicCore");
        }
    }

    /**
     * Populates the items in the spell inventory starting at index to all unlocked 'active' spells for the given
     * skill tree.
     *
     * @param treePosition (which of the three sub-trees?) (1, 2, 3)
     * @param index        which index to begin filling items
     */
    private int grabUnlockedSpellsFromTree(int treePosition, int index) {
        if (RunicCoreAPI.getSkillTree(player.getUniqueId(), treePosition) == null) return index;
        for (Perk perk : RunicCoreAPI.getSkillTree(player.getUniqueId(), treePosition).getPerks()) {
            if (perk.getCurrentlyAllocatedPoints() < perk.getCost()) continue;
            if (!(perk instanceof PerkSpell)) continue;
            if (RunicCoreAPI.getSpell(((PerkSpell) perk).getSpellName()) == null) continue;
            if (RunicCoreAPI.getSpell(((PerkSpell) perk).getSpellName()).isPassive()) continue;
            this.getInventory().setItem(index, SkillTreeGUI.buildPerkItem(perk, false, ChatColor.LIGHT_PURPLE + "» Click to activate"));
            index++;
        }
        return index;
    }
}