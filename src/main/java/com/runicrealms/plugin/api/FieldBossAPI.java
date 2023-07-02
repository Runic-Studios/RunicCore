package com.runicrealms.plugin.api;

import com.runicrealms.plugin.fieldboss.FieldBoss;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface FieldBossAPI {

    /**
     * Get all loaded field bosses, whether active or not.
     */
    Collection<FieldBoss> getFieldBosses();

    /**
     * Get a field boss by its identifier, null if none exists
     */
    @Nullable
    FieldBoss getFieldBoss(String identifier);


}
