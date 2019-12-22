package com.runicrealms.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * This custom event is called when a player receives a loot drop from any mob.
 * Used to communicate with loot bonus mechanics.
 */
public class LootEvent extends Event implements Cancellable {

    private Player player;
    private ItemStack itemStack;
    private boolean isCancelled;

    public LootEvent(Player damager, ItemStack itemStack) {
        this.player = damager;
        this.itemStack = itemStack;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ItemStack getCommand() {
        return this.itemStack;
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
