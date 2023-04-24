package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.Map;

public interface ShieldingSpell {

    double getShield();

    void setShield(double shield);

    double getShieldingPerLevel();

    /**
     * @param spellData key-value pairs map from yaml
     */
    default void loadShieldingData(Map<String, Object> spellData) {
        Number shield = (Number) spellData.getOrDefault("shield", 0);
        setShield(shield.doubleValue());
        Number shieldPerLevel = (Number) spellData.getOrDefault("shield-per-level", 0);
        setShieldPerLevel(shieldPerLevel.doubleValue());
    }

    void setShieldPerLevel(double shieldingPerLevel);

}
