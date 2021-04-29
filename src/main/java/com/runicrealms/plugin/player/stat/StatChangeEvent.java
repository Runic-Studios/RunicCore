package com.runicrealms.plugin.player.stat;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * This event is called whenever the base stats of a player are changed (login, buying stat points, gear, etc.)
 */
public class StatChangeEvent extends Event implements Cancellable {

    private final Player player;
    private final StatContainer statContainer;
    private boolean isCancelled;

    /**
     * Initialize Stat Change event with specified player and stats
     * @param player grab the player
     * @param statContainer container with player's base stats
     */
    public StatChangeEvent(Player player, StatContainer statContainer) {
        this.player = player;
        this.statContainer = statContainer;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return this.player;
    }

    public StatContainer getStatContainer() {
        return this.statContainer;
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
