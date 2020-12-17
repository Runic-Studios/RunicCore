package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.player.stat.BaseStatEnum;

public class PerkBaseStat extends Perk {

    private final int bonusAmount;
    private final BaseStatEnum baseStatEnum;

    public PerkBaseStat(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints,
                        BaseStatEnum baseStatEnum, int bonusAmount) {
        super(perkID, cost, currentlyAllocatedPoints, maxAllocatedPoints);
        this.baseStatEnum = baseStatEnum;
        this.bonusAmount = bonusAmount;
    }

    public int getBonusAmount() {
        return bonusAmount;
    }

    public BaseStatEnum getBaseStatEnum() {
        return baseStatEnum;
    }
}
