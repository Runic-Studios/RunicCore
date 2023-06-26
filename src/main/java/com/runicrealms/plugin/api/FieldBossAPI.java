package com.runicrealms.plugin.api;

import com.runicrealms.plugin.fieldboss.FieldBoss;

import java.util.Collection;

public interface FieldBossAPI {

    /**
     * Get all loaded field bosses, whether active or not.
     */
    Collection<FieldBoss> getFieldBosses();

    /**
     * Get a field boss by its identifier
     */
    FieldBoss getFieldBoss(String identifier);

}
