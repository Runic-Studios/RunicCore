package com.runicrealms.plugin.spellapi.spelltypes;

import java.util.Map;

public interface MagicDamageSpell {

    double getMagicDamage();

    void setMagicDamage(double magicDamage);

    double getMagicDamagePerLevel();

    void setMagicDamagePerLevel(double magicDamagePerLevel);

    /**
     * @param spellData key-value pairs map from yaml
     */
    default void loadMagicData(Map<String, Object> spellData) {
        Number magicDamage = (Number) spellData.getOrDefault("magic-damage", 0);
        setMagicDamage(magicDamage.doubleValue());
        Number magicDamagePerLevel = (Number) spellData.getOrDefault("magic-damage-per-level", 0);
        setMagicDamagePerLevel(magicDamagePerLevel.doubleValue());
    }


}
