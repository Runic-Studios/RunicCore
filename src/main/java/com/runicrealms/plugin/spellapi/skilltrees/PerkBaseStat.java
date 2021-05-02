package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.player.stat.PlayerStatEnum;

public class PerkBaseStat extends Perk {

    private static final int DEFAULT_BONUS = 2;
    private final int bonusAmount;
    private final PlayerStatEnum playerStatEnum;

    public PerkBaseStat(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints,
                        PlayerStatEnum playerStatEnum, int bonusAmount) {
        super(perkID, cost, currentlyAllocatedPoints, maxAllocatedPoints);
        this.playerStatEnum = playerStatEnum;
        this.bonusAmount = bonusAmount;
    }

    public PerkBaseStat(int perkID, int cost, int currentlyAllocatedPoints, int maxAllocatedPoints, PlayerStatEnum playerStatEnum) {
        super(perkID, cost, currentlyAllocatedPoints, maxAllocatedPoints);
        this.playerStatEnum = playerStatEnum;
        this.bonusAmount = DEFAULT_BONUS;
    }

    public int getBonusAmount() {
        return bonusAmount;
    }

    public PlayerStatEnum getPlayerStatEnum() {
        return playerStatEnum;
    }
}
