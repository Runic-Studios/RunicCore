package com.runicrealms.plugin.spellapi.skilltrees;

public class PerkSpell extends Perk {

    private String spellName;

    public PerkSpell(int cost, int minPointsReq, int maxAllocatedPoints, String spellName) {
        super(cost, minPointsReq, maxAllocatedPoints);
        this.spellName = spellName;
    }

    public String getSpellName() {
        return spellName;
    }

    public void setSpellName(String spellName) {
        this.spellName = spellName;
    }
}
