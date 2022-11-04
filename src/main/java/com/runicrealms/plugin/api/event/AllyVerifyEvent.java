package com.runicrealms.plugin.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player attempts to heal another player
 * Used to prevent healing enemy outlaws, duel targets, etc.
 */
public class AllyVerifyEvent extends Event implements Cancellable {

    private final Player caster;
    private final Entity recipient;
    private boolean isCancelled;

    /**
     * @param caster    player who cast heal spell
     * @param recipient entity that would receive heal
     */
    public AllyVerifyEvent(Player caster, Entity recipient) {
        this.caster = caster;
        this.recipient = recipient;
        this.isCancelled = false;
    }

    public Player getCaster() {
        return this.caster;
    }

    public Entity getRecipient() {
        return this.recipient;
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
