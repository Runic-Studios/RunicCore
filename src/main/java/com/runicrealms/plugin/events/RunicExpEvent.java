package com.runicrealms.plugin.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

/**
 * This custom event is called when a player gains experience from any source.
 * This gets called in our ClassExpCMD
 * @author Skyfallin
 */
public class RunicExpEvent extends Event implements Cancellable {

    public enum RunicExpSource {
        DUNGEON,
        MOB,
        OTHER, // custom
        PARTY, // exp from party kill
        QUEST
    }

    private final int originalAmount;
    private int finalAmount;
    private final Player player;
    private final RunicExpSource runicExpSource;
    private final int mobLevel;
    private final Location location;
    private boolean isCancelled;

    /**
     * Give a player experience through our custom calculators.
     * @param originalAmount original exp of event (cannot be modified)
     * @param finalAmount the exp the player will receive (with bonuses and modifiers)
     * @param player to receive experience
     * @param runicExpSource source of experience
     * @param mobLevel used to cap experience against low/high-level mobs
     * @param location of mob, used to spawn exp holograms
     */
    public RunicExpEvent(int originalAmount, int finalAmount, Player player, RunicExpSource runicExpSource, int mobLevel, @Nullable Location location) {
        this.originalAmount = originalAmount;
        this.finalAmount = finalAmount;
        this.player = player;
        this.runicExpSource = runicExpSource;
        this.mobLevel = mobLevel;
        this.location = location;
        this.isCancelled = false;
    }

    public int getOriginalAmount() {
        return originalAmount;
    }

    public int getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(int finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Player getPlayer() {
        return this.player;
    }

    public RunicExpSource getRunicExpSource() {
        return this.runicExpSource;
    }

    public int getMobLevel() {
        return mobLevel;
    }

    @Nullable
    public Location getLocation() {
        return this.location;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    private static final HandlerList handlers = new HandlerList();

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
