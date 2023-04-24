package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.Map;

public interface AttributeSpell {

    /**
     * Gets the 'floor' value of the bonus associated with the attribute
     *
     * @return the base value (e.g. 5)
     */
    double getBaseValue();

    void setBaseValue(double baseValue);

    /**
     * Gets the multiplier-per-stat value of the bonus associated with the stat
     *
     * @return the modifier (e.g. 0.25x stat)
     */
    double getMultiplier();

    void setMultiplier(double multiplier);

    /**
     * Gets the stat this should listen for
     *
     * @return the stat "intelligence"
     */
    String getStatName();

    void setStatName(String statName);

    /**
     * @param spellData key-value pairs map from yaml
     */
    default void loadAttributeData(Map<String, Object> spellData) {
        setStatName((String) spellData.getOrDefault("attribute", ""));
        Number baseValue = (Number) spellData.getOrDefault("attribute-base-value", 0);
        setBaseValue(baseValue.doubleValue());
        Number multiplier = (Number) spellData.getOrDefault("attribute-multiplier", 0);
        setMultiplier(multiplier.doubleValue());
    }
}
