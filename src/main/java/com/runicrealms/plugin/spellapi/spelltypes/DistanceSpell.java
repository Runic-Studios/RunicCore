package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.Map;

public interface DistanceSpell {

    /**
     * @return the distance of the spell's effect
     */
    double getDistance();

    void setDistance(double distance);

    /**
     * @param spellData key-value pairs map from yaml
     */
    default void loadDistanceData(Map<String, Object> spellData) {
        Number distance = (Number) spellData.getOrDefault("distance", 0);
        setDistance(distance.doubleValue());
    }

}
