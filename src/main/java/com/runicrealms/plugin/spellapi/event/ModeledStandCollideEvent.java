package com.runicrealms.plugin.spellapi.event;

import com.runicrealms.plugin.spellapi.armorstand.CollisionCause;
import com.runicrealms.plugin.spellapi.armorstand.ModeledStand;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom event is called when a custom ModeledItem collides with an entity
 */
public class ModeledStandCollideEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final ModeledStand modeledStand;
    private final Location location;
    private final CollisionCause collisionCause;
    private final LivingEntity entity;
    private boolean passThroughEnemies;
    private boolean isCancelled;

    public ModeledStandCollideEvent(ModeledStand modeledStand, CollisionCause collisionCause) {
        this.modeledStand = modeledStand;
        this.location = modeledStand.getLocation();
        this.collisionCause = collisionCause;
        this.entity = null;
        this.passThroughEnemies = false;
        this.isCancelled = false;
    }

    public ModeledStandCollideEvent(ModeledStand modeledStand, CollisionCause collisionCause, @NotNull LivingEntity entity) {
        this.modeledStand = modeledStand;
        this.location = modeledStand.getLocation();
        this.collisionCause = collisionCause;
        this.entity = entity;
        this.passThroughEnemies = false;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ModeledStand getModeledStand() {
        return modeledStand;
    }

    public Location getLocation() {
        return location;
    }

    public CollisionCause getCollisionCause() {
        return collisionCause;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public boolean shouldPassThroughEnemies() {
        return passThroughEnemies;
    }

    public void setPassThroughEnemies(boolean passThroughEnemies) {
        this.passThroughEnemies = passThroughEnemies;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
