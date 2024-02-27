package com.runicrealms.plugin.spellapi.modeled;

import com.ticxo.modelengine.api.model.ModeledEntity;

public class ModeledSpellAttached implements ModeledSpell {
    private final String modelId;
    private final ModeledSpellType modeledSpellType;
    private final double hitboxScale;
    private final ModeledEntity modeledEntity;
    private double duration;

    public ModeledSpellAttached(
            String modelId,
            ModeledSpellType modeledSpellType,
            double hitboxScale,
            ModeledEntity modeledEntity,
            double duration) {
        this.modelId = modelId;
        this.modeledSpellType = modeledSpellType;
        this.duration = duration;
        this.hitboxScale = hitboxScale;
        this.modeledEntity = modeledEntity;
    }

    @Override
    public String getModelId() {
        return modelId;
    }

    @Override
    public ModeledSpellType getModeledSpellType() {
        return modeledSpellType;
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
    public double getHitboxScale() {
        return hitboxScale;
    }

    @Override
    public ModeledEntity getModeledEntity() {
        return modeledEntity;
    }
}
