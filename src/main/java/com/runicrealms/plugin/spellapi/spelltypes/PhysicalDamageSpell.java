package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.Map;

public interface PhysicalDamageSpell {

    double getPhysicalDamage();

    void setPhysicalDamage(double physicalDamage);

    double getPhysicalDamagePerLevel();

    void setPhysicalDamagePerLevel(double physicalDamagePerLevel);

    /**
     * @param spellData key-value pairs map from yaml
     */
    default void loadPhysicalData(Map<String, Object> spellData) {
        Number physicalDamage = (Number) spellData.getOrDefault("physical-damage", 0);
        setPhysicalDamage(physicalDamage.doubleValue());
        Number physicalDamagePerLevel = (Number) spellData.getOrDefault("physical-damage-per-level", 0);
        setPhysicalDamagePerLevel(physicalDamagePerLevel.doubleValue());
    }

}
