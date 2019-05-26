package com.runicrealms.plugin.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player successfully melee attacks an enemy with their artifact,
 * i.e., it is not on cooldown. Called in DamageListener, rather than the util.
 */
public class WeaponDamageEvent extends Event implements Cancellable {

    private Player player;
    private Entity entity;
    private boolean isCancelled;

    public WeaponDamageEvent(Player damager, Entity victim) {
        this.player = damager;
        this.entity = victim;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getEntity() {
        return this.entity;
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
