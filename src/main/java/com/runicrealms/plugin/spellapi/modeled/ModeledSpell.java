package com.runicrealms.plugin.spellapi.modeled;

import com.runicrealms.plugin.RunicCore;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface ModeledSpell {

    /**
     * ?
     *
     * @return
     */
    Player getPlayer();

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
    Location getSpawnLocation();

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
    double getStartTime();

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

    /**
     * ?
     *
     * @param location
     * @return
     */
    Entity initializeBaseEntity(Location location);

    /**
     * ?
     *
     * @return
     */
    ModeledEntity spawnModel();

    /**
     * Initializes the modeled spell, tracking it and adding it to the manager
     */
    default void initialize() {
        RunicCore.getModeledSpellAPI().addModeledSpellToManager(this);
    }

    /**
     * ?
     */
    void cancel();
}
