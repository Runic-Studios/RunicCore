package com.runicrealms.plugin.spellapi.effect;

import com.runicrealms.plugin.RunicCore;
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
     * @param globalCounter checks the current global iterator counter value
     */
    void tick(int globalCounter);

    /**
     * Handles the logic for the actual mechanic
     */
    void executeSpellEffect();

    /**
     * Initializes the spell effect, tracking it and adding it to the manager
     */
    default void initialize() {
        RunicCore.getSpellEffectAPI().addSpellEffectToManager(this);
    }

    /**
     * Cancels the spell effect early, regardless of duration or ticks remaining
     */
    void cancel();
}

