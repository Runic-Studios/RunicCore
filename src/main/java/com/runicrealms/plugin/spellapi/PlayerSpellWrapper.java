package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import org.bukkit.entity.Player;

import java.util.HashSet;

/**
 *
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
    private static final String DEFAULT_ARCHER = "Barrage";
    private static final String DEFAULT_CLERIC = "Rejuvenate";
    private static final String DEFAULT_MAGE = "Fireball";
    private static final String DEFAULT_ROGUE = "Sprint";
    private static final String DEFAULT_WARRIOR = "Slam";

    /**
     * Constructs a spell wrapper for the given player with specified spells, which can be left blank.
     * @param player
     * @param spellHotbarOne
     * @param spellLeftClick
     * @param spellRightClick
     * @param spellSwapHands
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
     *
     * @param player
     * @param spells
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
        switch (RunicCoreAPI.getPlayerCache(player).getClassName()) {
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
