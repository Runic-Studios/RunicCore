package com.runicrealms.plugin.api;

import com.runicrealms.plugin.party.Party;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface PartyAPI {

    /**
     * Check whether player is eligible to join a party
     *
     * @param uuid of player to check
     * @return true if player can join party
     */
    boolean canJoinParty(UUID uuid);

    /**
     * @return a set of all parties
     */
    Set<Party> getParties();

    /**
     * Used to get the party of the given player, if it exists
     *
     * @param uuid of player to check
     * @return their party object
     */
    Party getParty(UUID uuid);

    /**
     * Returns simple boolean. If yes, player has party. If no, they don't.
     *
     * @param uuid of player to check for party
     * @return true if party, false if none
     */
    boolean hasParty(UUID uuid);

    /**
     * Used to determine whether two players are in a party.
     *
     * @param first  UUID of the first player
     * @param second The second player
     * @return boolean, whether they are in the same party
     */
    boolean isPartyMember(UUID first, Player second);

    /**
     * Checks whether the given player has any outstanding party invites
     *
     * @param player to check
     * @return true if the player has an invitation
     */
    boolean memberHasInvite(Player player);

    /**
     * Updates the map of player --> party with a new party object
     * Use null to remove player from party
     *
     * @param uuid  of the player key in the map
     * @param party the new party object
     */
    void updatePlayerParty(UUID uuid, Party party);
}
