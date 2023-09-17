package com.runicrealms.plugin.api;

import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface StatusEffectAPI {

    /**
     * Add a custom status effect to an entity.
     *
     * @param livingEntity      to receive status effect
     * @param runicStatusEffect which status effect to add
     * @param durationInSecs    (in seconds) of effect
     * @param displayMessage    if a message should be sent to the user that a status effect was applied
     * @param applier           the entity that applied the status effect
     */
    void addStatusEffect(@NotNull LivingEntity livingEntity, @NotNull RunicStatusEffect runicStatusEffect, double durationInSecs, boolean displayMessage, @Nullable LivingEntity applier);

    /**
     * Add a custom status effect to an entity.
     *
     * @param livingEntity      to receive status effect
     * @param runicStatusEffect which status effect to add
     * @param durationInSecs    (in seconds) of effect
     */
    void addStatusEffect(@NotNull LivingEntity livingEntity, @NotNull RunicStatusEffect runicStatusEffect, double durationInSecs, boolean displayMessage);

    /**
     * Cleanses all negative status effects from given uuid
     *
     * @param uuid to cleanse
     */
    void cleanse(@NotNull UUID uuid);

    /**
     * Purges all positive status effects from given uuid
     *
     * @param uuid to purge
     */
    void purge(@NotNull UUID uuid);

    /**
     * Check whether the given player is effected by the given status effect
     *
     * @param uuid              of player or entity to check
     * @param runicStatusEffect to lookup
     * @return true if the player is currently impacted by the effect
     */
    boolean hasStatusEffect(@NotNull UUID uuid, @NotNull RunicStatusEffect runicStatusEffect);

    /**
     * Removes the custom status effect (root, stun, etc.) from the specified player
     *
     * @param uuid         of the player to remove
     * @param statusEffect the status effect enum
     * @return true if an effect was removed
     */
    boolean removeStatusEffect(@NotNull UUID uuid, @NotNull RunicStatusEffect statusEffect);

    /**
     * Gets the duration of the provided status effect on the given entity in seconds
     *
     * @param uuid   the uuid of the given entity
     * @param effect the provided status effect
     * @return the duration of the provided status effect on the given entity in seconds
     */
    double getStatusEffectDuration(@NotNull UUID uuid, @NotNull RunicStatusEffect effect);
}
