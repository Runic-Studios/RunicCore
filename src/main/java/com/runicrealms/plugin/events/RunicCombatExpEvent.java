package com.runicrealms.plugin.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * This custom event is called when a player gains experience from any source.
 *
 * @author Skyfallin
 */
public class RunicCombatExpEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final int amount;
    private final boolean applyBonuses;
    private final Player player;
    private final RunicExpSource runicExpSource;
    private final @Nullable Location hologramLocation;
    private final Map<BonusType, Double> bonuses = new HashMap<>();
    private boolean isCancelled;

    /**
     * Give a player experience through our custom calculators.
     *
     * @param amount           initial amount of experience before bonuses
     * @param applyBonuses     whether this experience should be boosted by bonuses
     * @param player           to receive experience
     * @param runicExpSource   source of experience
     * @param hologramLocation location of the hologram for EXP
     */
    public RunicCombatExpEvent(final int amount, boolean applyBonuses, Player player, RunicExpSource runicExpSource, @Nullable Location hologramLocation) {
        this.amount = amount;
        this.applyBonuses = applyBonuses;
        this.player = player;
        this.runicExpSource = runicExpSource;
        this.isCancelled = false;
        this.hologramLocation = hologramLocation;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public int getRawAmount() {
        return amount;
    }

    public int getAmountNoBonuses() {
        return amount;
    }

    /**
     * Bonuses are a value starting from 0, where 0.25 would be an additional 25% EXP bonus.
     */
    public void setBonus(BonusType type, double bonus) {
        this.bonuses.put(type, bonus);
    }

    public int getExpFromBonus(BonusType type) {
        Double bonus = this.bonuses.get(type);
        if (bonus == null) return 0;
        return (int) Math.round(amount * bonus);
    }

    public int getFinalAmount() {
        return getFinalAmount(getAmountNoBonuses());
    }

    public int getFinalAmount(int expAmount) {
        if (!applyBonuses) return expAmount;
        return (int) Math.round(expAmount * (1 + this.bonuses.values().stream().mapToDouble((bonus) -> bonus).sum()));
    }

    public @Nullable Location getHologramLocation() {
        return this.hologramLocation;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public RunicExpSource getRunicExpSource() {
        return this.runicExpSource;
    }

    public boolean shouldApplyBonuses() {
        return this.applyBonuses;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    public enum RunicExpSource {
        DUNGEON,
        MOB,
        OTHER, // custom
        QUEST
    }

    public enum BonusType {
        BOOST,
        PARTY,
        WISDOM,
        GUILD,
        VOTE,
        OUTLAW
    }
}
