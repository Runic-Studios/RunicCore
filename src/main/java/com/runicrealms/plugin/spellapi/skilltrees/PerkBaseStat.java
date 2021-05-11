package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.runicitems.Stat;

public class PerkBaseStat extends Perk {

    private static final int DEFAULT_BONUS = 2;
    private final int bonusAmount;
    private final Stat stat;

    public PerkBaseStat(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints,
                        Stat stat, int bonusAmount) {
        super(perkID, cost, currentlyAllocatedPoints, maxAllocatedPoints);
        this.stat = stat;
        this.bonusAmount = bonusAmount;
    }

    public PerkBaseStat(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints, Stat stat) {
        super(perkID, cost, currentlyAllocatedPoints, maxAllocatedPoints);
        this.stat = stat;
        this.bonusAmount = DEFAULT_BONUS;
    }

    public int getBonusAmount() {
        return bonusAmount;
    }

    public Stat getStat() {
        return stat;
    }
}
