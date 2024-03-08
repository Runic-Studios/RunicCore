package com.runicrealms.plugin.spellapi.modeled;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

/**
 * The type Modeled spell stationary.
 */
public class ModeledSpellStationary implements ModeledSpell {
    protected final Player player;
    protected final String modelId;
    protected final Location spawnLocation;
    protected final double hitboxScale;
    protected final Entity entity;
    protected final ModeledEntity modeledEntity;
    protected final Predicate<Entity> validTargets;
    protected double startTime;
    protected double duration;

    /**
     * Instantiates a new Modeled spell stationary.
     *
     * @param player        the player
     * @param modelId       the model id
     * @param spawnLocation the spawn location
     * @param hitboxScale   the hitbox scale
     * @param duration      the duration
     * @param validTargets  the valid targets
     */
    public ModeledSpellStationary(
            final Player player,
            final String modelId,
            final Location spawnLocation,
            final double hitboxScale,
            final double duration,
            final Predicate<Entity> validTargets) {
        this.player = player;
        this.modelId = modelId;
        this.spawnLocation = spawnLocation;
        this.duration = duration;
        this.validTargets = validTargets;
        this.hitboxScale = hitboxScale;
        this.entity = initializeBaseEntity(this.spawnLocation);
        this.modeledEntity = spawnModel();
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public String getModelId() {
        return modelId;
    }

    @Override
    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getStartTime() {
        return startTime;
    }

    @Override
    public double getHitboxScale() {
        return hitboxScale;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public ModeledEntity getModeledEntity() {
        return modeledEntity;
    }

    @Override
    public Entity initializeBaseEntity(Location location) {
        ArmorStand armorStand = player.getWorld().spawn(location, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setCollidable(false);
        armorStand.setInvulnerable(true);
        armorStand.setMarker(true);
        return armorStand;
    }

    @Override
    public ModeledEntity spawnModel() {
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(this.modelId);
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);
        modeledEntity.setBaseEntityVisible(false);

        if (activeModel != null) {
            activeModel.setHitboxVisible(true);
            activeModel.setHitboxScale(this.hitboxScale);
            modeledEntity.addModel(activeModel, true);
        }

        return modeledEntity;
    }

    @Override
    public void cancel() {
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
    }

    @Override
    public Predicate<Entity> getValidTargets() {
        return validTargets;
    }
}
