package com.runicrealms.plugin.spellapi.modeled;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class ModeledSpellAttached implements ModeledSpell {
    private final Player player;
    private final String modelId;
    private final Location spawnLocation;
    private final double hitboxScale;
    private final Entity entity;
    private final ModeledEntity modeledEntity;
    private double startTime;
    private double duration;

    public ModeledSpellAttached(
            final Player player,
            final String modelId,
            final Location spawnLocation,
            final double hitboxScale,
            final double duration) {
        this.player = player;
        this.modelId = modelId;
        this.spawnLocation = spawnLocation;
        this.duration = duration;
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
        this.player.addPassenger(armorStand);
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
        player.removePassenger(this.entity);
        startTime = (long) (System.currentTimeMillis() - (duration * 1000)); // Immediately end effect
    }
}
