package com.runicrealms.plugin.donor.boost.api;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * API for managing players' boosts.
 * Warning: EVERYTHING HERE SHOULD BE CALLED ASYNC
 */
public interface BoostAPI {

    /**
     * Add a number of boosts to a given target with given type and count.
     * Does not activate immediately, instead will be added to their BoostUI
     */
    void addStoreBoost(UUID target, StoreBoost boost, int count);

    /**
     * Add a single boost to a given target with given type.
     * Does not activate immediately, instead will be added to their BoostUI
     */
    void addStoreBoost(UUID target, StoreBoost boost);

    /**
     * Activates a boost for a target immediately. Decrements their stored boost count by one.
     * This function takes a player since the activator must be online to activate the boost.
     * Throws an exception if the player has no stored boosts.
     */
    void activateStoreBoost(Player player, StoreBoost boost);

    /**
     * Activates a custom boost immediately.
     * DOES NOT decrement their stored boost count, since it isn't necessarily a StoreBoost.
     * This function takes a player since the activator must be online to activate the boost.
     */
    void activateBoost(Player activator, Boost boost);

    /**
     * Gets the number of boosts a target of a given type.
     */
    int getStoreBoostCount(UUID target, StoreBoost boost);

    /**
     * Checks if a given target has at least one boost of a given type.
     */
    boolean hasStoreBoost(UUID target, StoreBoost boost);

    /**
     * Gets all the current active boosters on the shard.
     */
    Collection<Boost> getCurrentActiveBoosts();

    /**
     * Checks if the following boost is active.
     */
    boolean isBoostActive(Boost boost);

    /**
     * Checks if we have already delayed an automatic runicrestart to accommodate for a booster being activated.
     * This is to prevent players from chaining boosters and keep delaying restarts, impacting server health.
     */
    boolean hasDelayedRestart();

    /**
     * Gets the current multiplier we are applying to a BoostExperienceType.
     * 0 if no booster is active with that type.
     * If there are multiple boosts active for the same type of boost experience, ADDS THEM.
     */
    double getAdditionalExperienceMultiplier(BoostExperienceType experienceType);

}
