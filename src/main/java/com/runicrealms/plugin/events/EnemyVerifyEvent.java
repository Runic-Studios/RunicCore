package com.runicrealms.plugin.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * This custom event is called when a spell attempts to activate the spell logic on an entity.
 * Used to prevent non-pvpers from taking damage/effects
 */
public class EnemyVerifyEvent extends Event implements Cancellable {

    private final Player caster;
    private final Entity victim;
    private boolean isCancelled;

    public EnemyVerifyEvent(Player damager, Entity victim) {
        this.caster = damager;
        this.victim = victim;
        this.isCancelled = false;
    }

    public Player getCaster() {
        return this.caster;
    }
    public Entity getVictim() {
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
