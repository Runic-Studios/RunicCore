package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.Map;

public interface RadiusSpell {

    /**
     * @return the radius of the spell's effect
     */
    double getRadius();

    void setRadius(double radius);

    /**
     * @param spellData key-value pairs map from yaml
     */
    default void loadRadiusData(Map<String, Object> spellData) {
        Number radius = (Number) spellData.getOrDefault("radius", 0);
        setRadius(radius.doubleValue());
    }


}
