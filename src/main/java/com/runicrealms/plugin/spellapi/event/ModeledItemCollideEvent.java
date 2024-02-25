package com.runicrealms.plugin.spellapi.event;

import com.runicrealms.plugin.spellapi.item.CollisionCause;
import com.runicrealms.plugin.spellapi.item.ModeledItem;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom event is called when a custom ModeledItem collides with an entity
 */
public class ModeledItemCollideEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final ModeledItem modeledItem;
    private final Location location;
    private final CollisionCause collisionCause;
    private final LivingEntity entity;
    private boolean passThroughEnemies;
    private boolean isCancelled;

    public ModeledItemCollideEvent(ModeledItem modeledItem, CollisionCause collisionCause) {
        this.modeledItem = modeledItem;
        this.location = modeledItem.getLocation();
        this.collisionCause = collisionCause;
        this.entity = null;
        this.passThroughEnemies = false;
        this.isCancelled = false;
    }

    public ModeledItemCollideEvent(ModeledItem modeledItem, CollisionCause collisionCause, @NotNull LivingEntity entity) {
        this.modeledItem = modeledItem;
        this.location = modeledItem.getLocation();
        this.collisionCause = collisionCause;
        this.entity = entity;
        this.passThroughEnemies = false;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ModeledItem getModeledItem() {
        return modeledItem;
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
