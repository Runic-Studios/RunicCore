package com.runicrealms.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player leaves combat
 */
public class LeaveCombatEvent extends Event implements Cancellable {

    private final Player player;
    private boolean isCancelled;

    /**
     * Create an event for leaving combat for our other plugins to listen to
     *
     * @param player the player who will be removed from combat
     */
    public LeaveCombatEvent(Player player) {
        this.player = player;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return this.player;
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
