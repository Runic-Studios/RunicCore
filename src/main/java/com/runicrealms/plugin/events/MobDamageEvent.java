package com.runicrealms.plugin.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom event is called when a player receives damage from a mob. Currently only applies to monsters.
 * Called in DamageListener, rather than the util.
 */
public class MobDamageEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;
    private final LivingEntity victim;
    private final boolean applyMechanics;
    private int amount;
    private boolean isCancelled;

    /**
     * @param amount         the amount to be dealt to the player
     * @param entity         the mob who damaged the player
     * @param victim         the player who suffered damage
     * @param applyMechanics whether to apply knockback
     */
    public MobDamageEvent(int amount, Entity entity, LivingEntity victim, boolean applyMechanics) {
        this.amount = amount;
        this.entity = entity;
        this.victim = victim;
        this.applyMechanics = applyMechanics;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public LivingEntity getVictim() {
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

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
