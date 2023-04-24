package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.Map;

public interface WarmupSpell {

    /**
     * @return the warmup of the spell (in seconds)
     */
    double getWarmup();

    void setWarmup(double warmup);

    /**
     * @param spellData key-value pairs map from yaml
     */
    default void loadWarmupData(Map<String, Object> spellData) {
        Number warmup = (Number) spellData.getOrDefault("warmup", 0);
        setWarmup(warmup.doubleValue());
    }

}
