package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.Map;

public interface HealingSpell {

    double getHeal();

    void setHeal(double heal);

    double getHealingPerLevel();

    void setHealingPerLevel(double healingPerLevel);

    /**
     * @param spellData key-value pairs map from yaml
     */
    default void loadHealingData(Map<String, Object> spellData) {
        Number heal = (Number) spellData.getOrDefault("heal", 0);
        setHeal(heal.doubleValue());
        Number healingPerLevel = (Number) spellData.getOrDefault("heal-per-level", 0);
        setHealingPerLevel(healingPerLevel.doubleValue());
    }

}
