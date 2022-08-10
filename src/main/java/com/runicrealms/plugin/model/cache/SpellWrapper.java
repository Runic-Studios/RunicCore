package com.runicrealms.plugin.model.cache;

import com.runicrealms.plugin.model.PlayerSpellData;

import java.util.UUID;

/**
 * A wrapper class used to memoize player spells and reduce redis calls
 */
public class SpellWrapper {
    private String spellHotbarOne;
    private String spellLeftClick;
    private String spellRightClick;
    private String spellSwapHands;

    public SpellWrapper(UUID uuid) {
        this.spellHotbarOne = PlayerSpellData.determineDefaultSpell(uuid);
        this.spellLeftClick = "";
        this.spellRightClick = "";
        this.spellSwapHands = "";
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
