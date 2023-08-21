package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SpellData {
    public static final String DEFAULT_ARCHER = "Barrage";
    public static final String DEFAULT_CLERIC = "Sacred Spring";
    public static final String DEFAULT_MAGE = "Fireball";
    public static final String DEFAULT_ROGUE = "Dash";
    public static final String DEFAULT_WARRIOR = "Slam";
    private String spellHotbarOne = "";
    private String spellLeftClick = "";
    private String spellRightClick = "";
    private String spellSwapHands = "";

    @SuppressWarnings("unused")
    public SpellData() {
        // Default constructor for Spring
    }

    /**
     * Used for creating spell data from scratch
     *
     * @param playerClass of the player's character
     */
    public SpellData(String playerClass) {
        this.spellHotbarOne = determineDefaultSpell(playerClass);
    }

    /**
     * Constructs a spell wrapper for the given player with specified spells, which can be left blank.
     *
     * @param spellHotbarOne  spell assigned to hotbar 1
     * @param spellLeftClick  spell assigned to left-click
     * @param spellRightClick spell assigned to right-click
     * @param spellSwapHands  spell assigned to swap hands
     */
    public SpellData(String spellHotbarOne, String spellLeftClick,
                     String spellRightClick, String spellSwapHands) {
        this.spellHotbarOne = spellHotbarOne;
        this.spellLeftClick = spellLeftClick;
        this.spellRightClick = spellRightClick;
        this.spellSwapHands = spellSwapHands;
    }

    /**
     * Determines the default starter spell for each class
     *
     * @param playerClass of the player's character
     * @return a string corresponding to the spell name of the starter spell
     */
    public static String determineDefaultSpell(String playerClass) {
        return switch (playerClass) {
            case "Archer" -> DEFAULT_ARCHER;
            case "Cleric" -> DEFAULT_CLERIC;
            case "Mage" -> DEFAULT_MAGE;
            case "Rogue" -> DEFAULT_ROGUE;
            case "Warrior" -> DEFAULT_WARRIOR;
            default -> "";
        };
    }

    public String getSpellHotbarOne() {
        return spellHotbarOne;
    }

    public void setSpellHotbarOne(String spellHotbarOne) {
        this.spellHotbarOne = spellHotbarOne;
    }

    public String getSpellLeftClick() {
        return spellLeftClick;
    }

    public void setSpellLeftClick(String spellLeftClick) {
        this.spellLeftClick = spellLeftClick;
    }

    public String getSpellRightClick() {
        return spellRightClick;
    }

    public void setSpellRightClick(String spellRightClick) {
        this.spellRightClick = spellRightClick;
    }

    public String getSpellSwapHands() {
        return spellSwapHands;
    }

    public void setSpellSwapHands(String spellSwapHands) {
        this.spellSwapHands = spellSwapHands;
    }

    @NotNull
    @Contract("_, _ -> this")
    public SpellData clean(@NotNull Map<SkillTreePosition, SkillTreeData> data, @NotNull String playerClass) {
        Spell one = RunicCore.getSpellAPI().getSpell(this.spellHotbarOne);
        Spell two = RunicCore.getSpellAPI().getSpell(this.spellLeftClick);
        Spell three = RunicCore.getSpellAPI().getSpell(this.spellRightClick);
        Spell four = RunicCore.getSpellAPI().getSpell(this.spellSwapHands);

        if (one == null || !this.unlockedSpell(one, data, playerClass)) {
            this.spellHotbarOne = "";
        }

        if (two == null || !this.unlockedSpell(two, data, playerClass)) {
            this.spellLeftClick = "";
        }

        if (three == null || !this.unlockedSpell(three, data, playerClass)) {
            this.spellRightClick = "";
        }

        if (four == null || !this.unlockedSpell(four, data, playerClass)) {
            this.spellSwapHands = "";
        }

        return this;
    }

    /**
     * Reset the spells to their defaults
     *
     * @param playerClass of the character from the core player data
     */
    public void resetSpells(String playerClass) {
        this.spellHotbarOne = determineDefaultSpell(playerClass);
        this.spellLeftClick = "";
        this.spellRightClick = "";
        this.spellSwapHands = "";
    }

    private boolean unlockedSpell(@NotNull Spell spell, @NotNull Map<SkillTreePosition, SkillTreeData> data, @NotNull String playerClass) {
        if (SpellData.determineDefaultSpell(playerClass).equals(spell.getName())) {
            return true;
        }

        for (SkillTreeData skillTree : data.values()) {
            if (skillTree.hasSpellUnlocked(spell)) {
                return true;
            }
        }

        return false;
    }
}
