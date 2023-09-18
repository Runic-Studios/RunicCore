package com.runicrealms.plugin.events;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This custom event is called when a player 'dies', or their health *would* drop below 0,
 * but we cancel the vanilla death to apply our own mechanics.
 */
public class RunicDeathEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player victim;
    private final Entity killer;
    private final Location location;
    private boolean isCancelled;

    /**
     * Create a RunicDeathEvent with the specified player and optional victim
     *
     * @param victim   who died
     * @param location where the victim died (needed in case they log out)
     * @param killer   (optional) killer for mob/player-related deaths
     */
    public RunicDeathEvent(@NotNull Player victim, @NotNull Location location, @Nullable Entity killer) {
        this.victim = victim;
        this.location = location;
        this.killer = killer;
        this.isCancelled = false;
    }

    public RunicDeathEvent(@NotNull Player victim, @NotNull Location location) {
        this(victim, location, null);
    }

    @Nullable
    public Entity getKiller() {
        return killer;
    }

    @NotNull
    public Location getLocation() {
        return location;
    }

    @NotNull
    public Player getVictim() {
        return victim;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
