package com.runicrealms.plugin.api;

import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public interface StatusEffectAPI {

    /**
     * Add a custom status effect to an entity.
     *
     * @param livingEntity      to receive status effect
     * @param runicStatusEffect which status effect to add
     * @param durationInSecs    (in seconds) of effect
     */
    void addStatusEffect(LivingEntity livingEntity, RunicStatusEffect runicStatusEffect, double durationInSecs, boolean displayMessage);

    /**
     * Check whether the given player is effected by the given status effect
     *
     * @param uuid              of player or entity to check
     * @param runicStatusEffect to lookup
     * @return true if the player is currently impacted by the effect
     */
    boolean hasStatusEffect(UUID uuid, RunicStatusEffect runicStatusEffect);

    /**
     * Removes the custom status effect (root, stun, etc.) from the specified player
     *
     * @param uuid         of the player to remove
     * @param statusEffect the status effect enum
     * @return true if an effect was removed
     */
    boolean removeStatusEffect(UUID uuid, RunicStatusEffect statusEffect);
}
