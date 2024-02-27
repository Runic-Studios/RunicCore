package com.runicrealms.plugin.spellapi.modeled;

import com.ticxo.modelengine.api.model.ModeledEntity;

public interface ModeledSpell {

    /**
     * ?
     *
     * @return
     */
    String getModelId();

    /**
     * ?
     *
     * @return
     */
    ModeledSpellType getModeledSpellType();

    /**
     * ?
     *
     * @return
     */
    double getDuration();

    /**
     * ?
     *
     * @param duration
     */
    void setDuration(double duration);

    /**
     * ?
     *
     * @return
     */
    double getHitboxScale();

    /**
     * ?
     *
     * @return
     */
    ModeledEntity getModeledEntity();
}
