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
        QUEST
    }

    private int amount;
    private final Player player;
    private final RunicExpSource runicExpSource;
    private final int mobLevel;
    private final Location location;
    private boolean isCancelled;

    /**
     * Give a player experience through our custom calculators.
     * @param amount of experience to receive (before bonuses)
     * @param player to receive experience
     * @param runicExpSource source of experience
     * @param mobLevel used to cap experience against low/high-level mobs
     * @param location of mob, used to spawn exp holograms
     */
    public RunicExpEvent(int amount, Player player, RunicExpSource runicExpSource, int mobLevel, @Nullable Location location) {
        this.amount = amount;
        this.player = player;
        this.runicExpSource = runicExpSource;
        this.mobLevel = mobLevel;
        this.location = location;
        this.isCancelled = false;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
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
