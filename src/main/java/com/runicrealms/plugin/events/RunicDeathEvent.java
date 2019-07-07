package com.runicrealms.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player 'dies', or their health *would* drop below 0,
 * but we cancel the vanilla death to apply our own mechanics.
 */
public class RunicDeathEvent extends Event implements Cancellable {

    private Player victim;
    private boolean isCancelled;

    public RunicDeathEvent(Player victim) {
        this.victim = victim;
        this.isCancelled = false;
    }

    public Player getVictim() {
        return this.victim;
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
