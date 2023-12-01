package com.runicrealms.plugin.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Superclass of all custom damage event mechanics on runic
 */
public abstract class RunicDamageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final UUID eventId;
    private final LivingEntity victim;
    private int amount;
    private boolean isCancelled;

    /**
     * Constructor of the superclass of all custom damage event mechanics on runic
     *
     * @param victim the entity that received the damage
     * @param amount the amount of damage dealt
     */
    public RunicDamageEvent(LivingEntity victim, int amount) {
        this.eventId = UUID.randomUUID();
        this.victim = victim;
        this.amount = amount;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * A unique id that is generated for each damage event.
     * Useful for executing code for each unique damage event
     *
     * @return a random uuid
     */
    public UUID getEventId() {
        return eventId;
    }

    /**
     * A method that returns the entity that received the damage
     *
     * @return the entity that received the damage
     */
    public LivingEntity getVictim() {
        return this.victim;
    }

    /**
     * A method that returns the damage dealt
     *
     * @return the damage dealt
     */
    public int getAmount() {
        return this.amount;
    }

    /**
     * A method that is used to set the amount of damage dealt
     *
     * @param amount the amount of damage dealt
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
}
