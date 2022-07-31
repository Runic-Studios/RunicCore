package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.redis.RedisField;
import org.bukkit.entity.Player;

import java.util.HashSet;

/**
 * A wrapper to store the assignment of spells to each spell slot
 */
public class PlayerSpellWrapper {

    private Player player;
    private String spellHotbarOne;
    private String spellLeftClick;
    private String spellRightClick;
    private String spellSwapHands;
    private final HashSet<String> passives;
    public static final String PATH_1 = "hotBarOne";
    public static final String PATH_2 = "leftClick";
    public static final String PATH_3 = "rightClick";
    public static final String PATH_4 = "swapHands";
    public static final String DEFAULT_ARCHER = "Barrage";
    public static final String DEFAULT_CLERIC = "Holy Water";
    public static final String DEFAULT_MAGE = "Fireball";
    public static final String DEFAULT_ROGUE = "Sprint";
    public static final String DEFAULT_WARRIOR = "Slam";

    /**
     * Constructs a spell wrapper for the given player with specified spells, which can be left blank.
     *
     * @param player          to generate wrapper for
     * @param spellHotbarOne  spell assigned to hotbar 1
     * @param spellLeftClick  spell assigned to left-click
     * @param spellRightClick spell assigned to right-click
     * @param spellSwapHands  spell assigned to swap hands
     */
    public PlayerSpellWrapper(Player player, String spellHotbarOne, String spellLeftClick,
                              String spellRightClick, String spellSwapHands) {
        this.player = player;
        this.spellHotbarOne = spellHotbarOne;
        this.spellLeftClick = spellLeftClick;
        this.spellRightClick = spellRightClick;
        this.spellSwapHands = spellSwapHands;
        passives = new HashSet<>();
        RunicCore.getSkillTreeManager().getPlayerSpellWrappers().add(this);
    }

    /**
     * Constructs a player spell wrapper from DB
     *
     * @param player player to generate wrapper for
     * @param spells spells section of DB for character
     */
    public PlayerSpellWrapper(Player player, PlayerMongoDataSection spells) {
        this.player = player;
        this.spellHotbarOne = spells.get(PATH_1, String.class);
        this.spellLeftClick = spells.get(PATH_2, String.class);
        this.spellRightClick = spells.get(PATH_3, String.class);
        this.spellSwapHands = spells.get(PATH_4, String.class);
        passives = new HashSet<>();
        // call each skill tree and populate
        RunicCoreAPI.getSkillTree(player, 1).applyPassives(this);
        RunicCoreAPI.getSkillTree(player, 2).applyPassives(this);
        RunicCoreAPI.getSkillTree(player, 3).applyPassives(this);
        RunicCore.getSkillTreeManager().getPlayerSpellWrappers().add(this);
    }

    /**
     * Reset in-memory spells for player, such as a skill point reset.
     */
    public void clearSpells() {
        this.spellHotbarOne = determineDefaultSpell(player);
        this.spellLeftClick = "";
        this.spellRightClick = "";
        this.spellSwapHands = "";
        this.passives.clear();
    }

    public static String determineDefaultSpell(Player player) {
        switch (RunicCoreAPI.getRedisValue(player, RedisField.CLASS_TYPE)) {
            case "Archer":
                return DEFAULT_ARCHER;
            case "Cleric":
                return DEFAULT_CLERIC;
            case "Mage":
                return DEFAULT_MAGE;
            case "Rogue":
                return DEFAULT_ROGUE;
            case "Warrior":
                return DEFAULT_WARRIOR;
            default:
                return "";
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
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

    public HashSet<String> getPassives() {
        return passives;
    }
}
