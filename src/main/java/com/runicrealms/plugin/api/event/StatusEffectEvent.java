package com.runicrealms.plugin.api.event;

import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called whenever a custom RunicStatusEffect is applied (stun, root, etc.)
 */
public class StatusEffectEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity livingEntity;
    private final RunicStatusEffect runicStatusEffect;
    private final double durationInSeconds;
    private final boolean displayMessage;
    private boolean isCancelled;

    /**
     * @param livingEntity      entity who gained the status effect
     * @param runicStatusEffect that was applied
     * @param durationInSeconds the duration of the effect
     * @param displayMessage    whether to display the chat message
     */
    public StatusEffectEvent(LivingEntity livingEntity, RunicStatusEffect runicStatusEffect, double durationInSeconds, boolean displayMessage) {
        this.livingEntity = livingEntity;
        this.runicStatusEffect = runicStatusEffect;
        this.durationInSeconds = durationInSeconds;
        this.displayMessage = displayMessage;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public double getDurationInSeconds() {
        return durationInSeconds;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public LivingEntity getLivingEntity() {
        return livingEntity;
    }

    public RunicStatusEffect getRunicStatusEffect() {
        return runicStatusEffect;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    public boolean willDisplayMessage() {
        return displayMessage;
    }
}
