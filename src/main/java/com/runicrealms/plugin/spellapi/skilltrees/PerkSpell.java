package com.runicrealms.plugin.spellapi.skilltrees;

public class PerkSpell extends Perk {

    private final String spellName;

    public PerkSpell(int cost, int minPointsReq, int currentlyAllocatedPoints,
                     int maxAllocatedPoints, String spellName) {
        super(cost, minPointsReq, currentlyAllocatedPoints, maxAllocatedPoints);
        this.spellName = spellName;
    }

    public String getSpellName() {
        return spellName;
    }
}
