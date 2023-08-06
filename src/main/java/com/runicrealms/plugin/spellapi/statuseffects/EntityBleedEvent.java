package com.runicrealms.plugin.spellapi.statuseffects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A method that is called when an entity takes damage from bleeding
 *
 * @author BoBoBalloon
 */
public class EntityBleedEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final LivingEntity target;
    private double damage;
    private double levelMultiplier;
    private boolean cancelled;

    public EntityBleedEvent(@NotNull LivingEntity target) {
        this.target = target;
        this.damage = 1;
        this.levelMultiplier = .25;
        this.cancelled = false;
    }

    @NotNull
    public LivingEntity getTarget() {
        return this.target;
    }

    public double getDamage() {
        return this.damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getLevelMultiplier() {
        return this.levelMultiplier;
    }

    public void setLevelMultiplier(double levelMultiplier) {
        this.levelMultiplier = levelMultiplier;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
