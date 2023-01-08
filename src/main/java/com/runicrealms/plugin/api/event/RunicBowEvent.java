package com.runicrealms.plugin.api.event;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called when an Archer fires a custom arrow for their basic attack
 */
public class RunicBowEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private Arrow arrow;
    private boolean isCancelled;

    /**
     * @param player who fired the arrow
     * @param arrow  that was fired
     */
    public RunicBowEvent(Player player, Arrow arrow) {
        this.player = player;
        this.arrow = arrow;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Arrow getArrow() {
        return arrow;
    }

    public void setArrow(Arrow arrow) {
        this.arrow = arrow;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }
}
