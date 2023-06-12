package com.runicrealms.plugin.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * A version of the runic combat exp event that is fired for when we kill mobs.
 */
public class RunicMobCombatExpEvent extends RunicCombatExpEvent {

    private final int mobLevel;

    /**
     * Give a player experience through our custom calculators.
     *
     * @param amount       initial amount of experience before bonuses
     * @param applyBonuses whether this experience should be boosted by bonuses
     * @param player       to receive experience
     * @param mobLevel     used to cap experience against low/high-level mobs
     */
    public RunicMobCombatExpEvent(int amount, boolean applyBonuses, Player player, int mobLevel, @Nullable Location hologramLocation) {
        super(amount, applyBonuses, player, RunicExpSource.MOB, hologramLocation);
        this.mobLevel = mobLevel;
    }

    private static int calculateLevelDiff(int exp, int levelOne, int levelTwo) {
        int levelDiff = Math.abs(levelOne - levelTwo);
        if (levelDiff <= 5) {
            return exp;
        } else if (levelDiff <= 10) {
            return (int) Math.round(exp * 0.75);
        } else if (levelDiff <= 15) {
            return (int) Math.round(exp * 0.25);
        }
        return 0;
    }

    public int getMobLevel() {
        return this.mobLevel;
    }

    @Override
    public int getAmountNoBonuses() {
        return calculateLevelDiff(this.getRawAmount(), this.getPlayer().getLevel(), mobLevel);
    }

    @Override
    public int getFinalAmount() {
        return super.getFinalAmount(calculateLevelDiff(this.getRawAmount(), this.getPlayer().getLevel(), mobLevel));
    }

    @Override
    public int getExpFromBonus(BonusType type) {
        Double bonus = bonuses.get(type);
        if (bonus == null) return 0;
        return (int) Math.round(calculateLevelDiff(this.getRawAmount(), this.getPlayer().getLevel(), mobLevel) * bonus);
    }

}
