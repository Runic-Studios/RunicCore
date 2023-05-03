package com.runicrealms.plugin.api.event;

import com.runicrealms.plugin.spellapi.spelltypes.Shield;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom ASYNC event is called when a player's spell shield is broken
 */
public class ShieldBreakEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Shield shield;
    private boolean isCancelled;

    /**
     * @param player whose shield was broken
     * @param shield container that broke
     */
    public ShieldBreakEvent(Player player, @NotNull Shield shield) {
        super(true);
        this.player = player;
        this.shield = shield;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Shield getShield() {
        return this.shield;
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
