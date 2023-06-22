package com.runicrealms.plugin.spellapi.skilltrees.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.model.SpellField;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.spellapi.skilltrees.util.ArcherTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.ClericTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.MageTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.RogueTreeUtil;
import com.runicrealms.plugin.spellapi.skilltrees.util.WarriorTreeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SpellGUI implements InventoryHolder {

    private final Inventory inventory;
    private final Player player;
    private final SpellField spellField;

    public SpellGUI(Player player, SpellField spellField) {
        this.inventory = Bukkit.createInventory(this, 54, ColorUtil.format("&a&lAvailable Spells"));
        this.player = player;
        this.spellField = spellField;
        openMenu();
    }

    /**
     * Returns a dummy 'perk' that is used to represent the default spell for each class.
     *
     * @return a perk that can be used to build an item stack
     */
    private Perk determineDefaultSpellPerk() {
        return switch (RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player)) {
            case "Archer" -> ArcherTreeUtil.DEFAULT_ARCHER_SPELL_PERK;
            case "Cleric" -> ClericTreeUtil.DEFAULT_CLERIC_SPELL_PERK;
            case "Mage" -> MageTreeUtil.DEFAULT_MAGE_SPELL_PERK;
            case "Rogue" -> RogueTreeUtil.DEFAULT_ROGUE_SPELL_PERK;
            case "Warrior" -> WarriorTreeUtil.DEFAULT_WARRIOR_SPELL_PERK;
            default ->
                    throw new IllegalStateException("Unexpected value: getting default spell perk. Check SpellGUI.java in RunicCore");
        };
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public Player getPlayer() {
        return this.player;
    }

    public SpellField getSpellField() {
        return this.spellField;
    }

    /**
     * Populates the items in the spell inventory starting at index to all unlocked 'active' spells for the given
     * skill tree.
     *
     * @param treePosition (which of the three sub-trees?) (1, 2, 3)
     */
    private void grabUnlockedSpellsFromTree(SkillTreePosition treePosition) {
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        if (RunicCore.getSkillTreeAPI().getSkillTreeDataMap(player.getUniqueId(), slot) == null) return;
        if (RunicCore.getSkillTreeAPI().getSkillTreeDataMap(player.getUniqueId(), slot).get(treePosition) == null)
            return;
        for (Perk perk : RunicCore.getSkillTreeAPI().getSkillTreeDataMap(player.getUniqueId(), slot).get(treePosition).getPerks()) {
            if (perk.getCurrentlyAllocatedPoints() < perk.getCost()) continue;
            if (!(perk instanceof PerkSpell)) continue;
            if (RunicCore.getSpellAPI().getSpell(((PerkSpell) perk).getSpellName()) == null)
                continue;
            if (RunicCore.getSpellAPI().getSpell(((PerkSpell) perk).getSpellName()).isPassive())
                continue;
            this.getInventory().setItem(this.inventory.firstEmpty(), SkillTreeGUI.buildPerkItem(perk, false, ChatColor.LIGHT_PURPLE + "» Click to activate"));
        }
    }

    /**
     * Opens the inventory associated w/ this GUI, ordering perks
     */
    private void openMenu() {
        this.inventory.clear();
        GUIUtil.fillInventoryBorders(this.inventory);
        this.inventory.setItem(0, GUIUtil.BACK_BUTTON);
        this.inventory.setItem(this.inventory.firstEmpty(), SkillTreeGUI.buildPerkItem(determineDefaultSpellPerk(),
                false, ChatColor.LIGHT_PURPLE + "» Click to activate"));
        grabUnlockedSpellsFromTree(SkillTreePosition.FIRST);
        grabUnlockedSpellsFromTree(SkillTreePosition.SECOND);
        grabUnlockedSpellsFromTree(SkillTreePosition.THIRD);
    }
}