package com.runicrealms.plugin.spellapi.effect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface SpellEffect {


    /**
     * @return a unique string to identify the effect type (e.g. Bleed)
     */
    SpellEffectType getEffectType();

    /**
     * @return true if the effect should be currently active
     */
    boolean isActive(); // Is the effect currently active?

    /**
     * @return true if this is a positive effect
     */
    boolean isBuff();

    /**
     * @return the server time this effect was applied
     */
    default long getStartTime() {
        return 0;
    }

    /**
     * @return the duration of this effect (in seconds)
     */
    default double getDuration() {
        return 0;
    }

    /**
     * @return the player that applied this effect
     */
    Player getCaster();

    /**
     * @return the entity that is affected
     */
    LivingEntity getRecipient();

    /**
     * @param counter checks the current iterator counter
     */
    void tick(int counter);

    /**
     * @return the interval at which this specific effect should tick (longer for counter-based effects like Bleed)
     */
    int getTickInterval();

}

