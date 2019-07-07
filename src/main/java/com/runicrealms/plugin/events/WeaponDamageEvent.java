package com.runicrealms.plugin.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player successfully weapon attacks an enemy with their artifact,
 * i.e., it is not on cooldown. Called in DamageListener, rather than the util.
 * Can specify the cause of the event, ranged or melee, for use with on-hit runic spells.
 */
public class WeaponDamageEvent extends Event implements Cancellable {

    private int amount;
    private Player player;
    private Entity entity;
    private boolean isRanged;
    private boolean isCancelled;

    public WeaponDamageEvent(int amount, Player damager, Entity victim, boolean isRanged) {
        this.amount = amount;
        this.player = damager;
        this.entity = victim;
        this.isRanged = isRanged;
        this.isCancelled = false;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public boolean getIsRanged() {
        return this.isRanged;
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
