package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;

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

    /**
     * Used for AttributeSpells that offer percent increases. Given a player, stat, base value, and multiplier,
     * calculates the percent increase to be used in the spell effect
     *
     * @param player to check attributes for
     * @return the final percent modifier
     */
    default double percentAttribute(Player player) {
        double statValue = RunicCore.getStatAPI().getStat(player.getUniqueId(), this.getStatName());
        return Math.max(0, this.getBaseValue() + (this.getMultiplier() * statValue)); // Cannot be a negative bonus!
    }
}
