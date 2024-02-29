package com.runicrealms.plugin.spellapi.modeled;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class ModeledSpellProjectile implements ModeledSpell {
    private final Player player;
    private final String modelId;
    private final Location spawnLocation;
    private final double hitboxScale;
    private final Entity entity;
    private final ModeledEntity modeledEntity;
    private final Vector vector;
    private final Predicate<Entity> filter;
    private double startTime;
    private double duration;

    public ModeledSpellProjectile(
            final Player player,
            final String modelId,
            final Location spawnLocation,
            final Vector vector,
            final double hitboxScale,
            final double duration,
            final Predicate<Entity> filter) {
        this.player = player;
        this.modelId = modelId;
        this.spawnLocation = spawnLocation;
        this.vector = vector;
        this.hitboxScale = hitboxScale;
        this.duration = duration;
        this.filter = filter;
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

    public Vector getVector() {
        return vector;
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
        Pig entity = player.getWorld().spawn(location, Pig.class);
        entity.setGravity(false);
        entity.setInvisible(true);
        entity.setCollidable(false);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        entity.setVelocity(this.vector);
        return entity;
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
    public Predicate<Entity> getFilter() {
        return filter;
    }
}
