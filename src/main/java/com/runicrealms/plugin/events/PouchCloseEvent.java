package com.runicrealms.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * This custom event is called when a player closes a GUI item pouch.
 */
public class PouchCloseEvent extends Event implements Cancellable {

    private final Player player;
    private final ItemStack pouch;
    private int amount;
    private boolean isCancelled;

    public PouchCloseEvent(Player player, ItemStack pouch, int amount) {
        this.player = player;
        this.pouch = pouch;
        this.amount = amount;
        this.isCancelled = false;
    }

    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
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

    public Player getPlayer() {
        return player;
    }

    public ItemStack getPouch() {
        return pouch;
    }
}
