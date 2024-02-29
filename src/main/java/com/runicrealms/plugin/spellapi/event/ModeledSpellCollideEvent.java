package com.runicrealms.plugin.spellapi.event;

import com.runicrealms.plugin.spellapi.modeled.CollisionCause;
import com.runicrealms.plugin.spellapi.modeled.ModeledSpell;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom event is called when a custom ModeledSpell collides with an entity
 */
public class ModeledSpellCollideEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final ModeledSpell modeledSpell;
    private final Location location;
    private final CollisionCause collisionCause;
    private final LivingEntity entity;
    private boolean passThroughEnemies;
    private boolean isCancelled;

    public ModeledSpellCollideEvent(ModeledSpell modeledSpell, CollisionCause collisionCause) {
        this.modeledSpell = modeledSpell;
        this.location = modeledSpell.getEntity().getLocation();
        this.collisionCause = collisionCause;
        this.entity = null;
        this.passThroughEnemies = false;
        this.isCancelled = false;
    }

    public ModeledSpellCollideEvent(ModeledSpell modeledStand, CollisionCause collisionCause, @NotNull LivingEntity entity) {
        this.modeledSpell = modeledStand;
        this.location = modeledStand.getEntity().getLocation();
        this.collisionCause = collisionCause;
        this.entity = entity;
        this.passThroughEnemies = false;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ModeledSpell getModeledSpell() {
        return modeledSpell;
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
