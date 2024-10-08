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
    private final int damage;
    private final int maxDamage;
    private double cooldownTicks; // This is a double so that we don't have round each time we modify it, but we round at the end
    private boolean isCancelled;

    public BasicAttackEvent(Player player, Material material, int originalCooldownTicks, int cooldownTicks, int damage, int maxDamage) {
        this.player = player;
        this.material = material;
        this.originalCooldownTicks = originalCooldownTicks;
        this.cooldownTicks = cooldownTicks;
        this.damage = damage;
        this.maxDamage = maxDamage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public int getDamage() {
        return damage;
    }

    public int getMaxDamage() {
        return maxDamage;
    }

    // This is used so that we can stack cooldown modifiers without having to round between each time we stack
    public double getUnroundedCooldownTicks() {
        return cooldownTicks;
    }

    public int getRoundedCooldownTicks() {
        return (int) Math.round(cooldownTicks);
    }

    public void setCooldownTicks(double cooldownTicks) {
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
