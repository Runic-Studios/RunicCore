package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.Map;

public interface DurationSpell {

    /**
     * @return the duration of the spell (in seconds)
     */
    double getDuration();

    void setDuration(double duration);

    /**
     * @param spellData key-value pairs map from yaml
     */
    default void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
    }

}
