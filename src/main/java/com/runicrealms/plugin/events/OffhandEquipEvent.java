package com.runicrealms.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * This event is fired if the player modifies the item in their offhand.
 * If you want to get a RunicItem from this event, just use the RunicItemsAPI
 */
public class OffhandEquipEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final ItemStack item;
    private boolean cancelled = false;

    /**
     * Create an OffhandEquipEvent with the player equipping it and the ItemStack equipped.
     */
    public OffhandEquipEvent(Player player, ItemStack item) {
        this.player = player;
        this.item = item;
    }

    /**
     * Player equipping the offhand
     * @return Player
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * The offhand ItemStack equipped.
     * Returns null if removing an offhand.
     * @return Offhand ItemStack
     */
    public ItemStack getOffhandItem() {
        return this.item;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}

