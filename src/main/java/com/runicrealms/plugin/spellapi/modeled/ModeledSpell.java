package com.runicrealms.plugin.spellapi.modeled;

import com.runicrealms.plugin.RunicCore;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

public interface ModeledSpell {

    /**
     * @return the player who cast the spell
     */
    Player getPlayer();

    /**
     * @return the string identifier of the model in ModelEngine
     */
    String getModelId();

    /**
     * @return the location where the model will spawn
     */
    Location getSpawnLocation();

    /**
     * @return the duration before the model is forcibly removed
     */
    double getDuration();

    /**
     * @param duration to set before model is removed
     */
    void setDuration(double duration);

    /**
     * @return the time when the model was created
     */
    double getStartTime();

    /**
     * @return the hitbox scale
     */
    double getHitboxScale();

    /**
     * @return the modeled entity
     */
    ModeledEntity getModeledEntity();

    /**
     * Sets up the base entity which underlies the model
     *
     * @param location to spawn the entity
     * @return the base entity
     */
    Entity initializeBaseEntity(Location location);

    /**
     * @return the base entity
     */
    Entity getEntity();

    /**
     * @return setup and spawn the model over the base entity
     */
    ModeledEntity spawnModel();

    /**
     * Initializes the modeled spell, tracking it and adding it to the manager
     */
    default void initialize() {
        RunicCore.getModeledSpellAPI().addModeledSpellToManager(this);
    }

    /**
     * Immediately set the duration to max, expiring the model
     */
    void cancel();

    /**
     * Destroy the model and its underlying base entity
     */
    default void destroy() {
        getModeledEntity().destroy();
        getEntity().remove();
    }

    /**
     * A predicate filter when tracking nearby entities. Useful for only targeting enemies, allies, etc.
     *
     * @return an entity filter
     */
    Predicate<Entity> getFilter();
}
