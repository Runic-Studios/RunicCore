package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import org.bukkit.entity.Player;

/**
 *
 */
public class PlayerSpellWrapper {

    private Player player;
    private String spellHotbarOne;
    private String spellLeftClick;
    private String spellRightClick;
    private String spellSwapHands;
    public static final String PATH_1 = "hotBarOne";
    public static final String PATH_2 = "leftClick";
    public static final String PATH_3 = "rightClick";
    public static final String PATH_4 = "swapHands";
    public static final String DEFAULT_ARCHER = "Barrage";
    public static final String DEFAULT_CLERIC = "Rejuvenate";
    public static final String DEFAULT_MAGE = "Fireball";
    public static final String DEFAULT_ROGUE = "Sprint";
    public static final String DEFAULT_WARRIOR = "Slam";

    /**
     *
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
        RunicCore.getSkillTreeManager().getPlayerSpellWrappers().add(this);
    }

    /**
     * Reset in-memory spells for player, such as a skill point reset.
     */
    public void clearSpells() {
        switch (RunicCoreAPI.getPlayerCache(player).getClassName()) {
            case "Archer":
                this.spellHotbarOne = DEFAULT_ARCHER;
                break;
            case "Cleric":
                this.spellHotbarOne = DEFAULT_CLERIC;
                break;
            case "Mage":
                this.spellHotbarOne = DEFAULT_MAGE;
                break;
            case "Rogue":
                this.spellHotbarOne = DEFAULT_ROGUE;
                break;
            case "Warrior":
                this.spellHotbarOne = DEFAULT_WARRIOR;
                break;
        }
        this.spellLeftClick = "";
        this.spellRightClick = "";
        this.spellSwapHands = "";
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
}
