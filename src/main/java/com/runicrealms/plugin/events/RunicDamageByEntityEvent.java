package com.runicrealms.plugin.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Superclass of all custom damage event mechanics where the cause was another entity on runic
 */
public abstract class RunicDamageByEntityEvent extends RunicDamageEvent {
    private final LivingEntity damager;

    /**
     * Constructor of the superclass of all custom damage event mechanics where the cause was another entity on runic
     *
     * @param damager the entity that dealt the damage
     */
    public RunicDamageByEntityEvent(LivingEntity damager, LivingEntity victim, int amount) {
        super(victim, amount);
        this.damager = damager;
    }

    /**
     * A method that returns the entity that dealt the damage
     *
     * @return the entity that dealt the damage
     */
    public LivingEntity getDamager() {
        return this.damager;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
