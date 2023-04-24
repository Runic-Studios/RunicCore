package com.runicrealms.plugin.api.event;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called when a player triggers their basic attack
 */
public class BasicAttackEvent extends Event implements Cancellable {
    public static final int MINIMUM_COOLDOWN_TICKS = 5; // Four attacks per second
    public static final int BASE_MELEE_COOLDOWN = 10;
    public static final int BASE_BOW_COOLDOWN = 15;
    public static final int BASE_STAFF_COOLDOWN = 15;
    private static final HandlerList handlers = new HandlerList();
    private final int originalCooldownTicks; // Used as a reference to the original value
    private final Player player;
    private final Material material;
    private int cooldownTicks;
    private boolean isCancelled;

    public BasicAttackEvent(Player player, Material material, int originalCooldownTicks, int cooldownTicks) {
        this.player = player;
        this.material = material;
        this.originalCooldownTicks = originalCooldownTicks;
        this.cooldownTicks = cooldownTicks;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public void setCooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Material getMaterial() {
        return material;
    }

    public int getOriginalCooldownTicks() {
        return originalCooldownTicks;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }
}
