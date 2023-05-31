package com.runicrealms.plugin.api;

import com.runicrealms.plugin.player.CombatManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface CombatAPI {

    /**
     * Adds a player to the combat set, sends them a message
     *
     * @param uuid of the player to add
     */
    void enterCombat(UUID uuid, CombatManager.CombatType combatType);

    /**
     * Gives the specified player exp toward their combat level
     *
     * @param player to grant experience to
     * @param exp    amount of experience to give
     */
    void giveCombatExp(Player player, int exp);

    /**
     * Check if the current player is in combat
     *
     * @param uuid of player to check
     * @return true if player in combat
     */
    boolean isInCombat(UUID uuid);

    /**
     * Removes the specified player from combat
     *
     * @param uuid of player to remove from combat
     */
    void leaveCombat(UUID uuid);

}
