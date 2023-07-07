package com.runicrealms.plugin.model;

public class SpellData {
    public static final String DEFAULT_ARCHER = "Barrage";
    public static final String DEFAULT_CLERIC = "Sacred Spring";
    public static final String DEFAULT_MAGE = "Fireball";
    public static final String DEFAULT_ROGUE = "Leap";
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

}
