package com.runicrealms.plugin.spellapi.skilltrees;

public class PerkSpell extends Perk {

    private final String spellName;

    public PerkSpell(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints, String spellName) {
        super(perkID, cost, currentlyAllocatedPoints, maxAllocatedPoints);
        this.spellName = spellName;
    }

    public String getSpellName() {
        return spellName;
    }
}
