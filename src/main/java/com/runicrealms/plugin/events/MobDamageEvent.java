package com.runicrealms.plugin.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player receives damage from a mob. Currently only applies to monsters.
 * Called in DamageListener, rather than the util.
 */
public class MobDamageEvent extends Event implements Cancellable {

    private int amount;
    private final Entity damager;
    private final Entity victim;
    private final boolean applyMechanics;
    private boolean isCancelled;

    public MobDamageEvent(int amount, Entity damager, Entity victim, boolean applyMechanics) {
        this.amount = amount;
        this.damager = damager;
        this.victim = victim;
        this.applyMechanics = applyMechanics;
        this.isCancelled = false;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Entity getDamager() {
        return this.damager;
    }

    public Entity getVictim() {
        return this.victim;
    }

    public boolean shouldApplyMechanics() {
        return this.applyMechanics;
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
