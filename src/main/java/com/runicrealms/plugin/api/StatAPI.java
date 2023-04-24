package com.runicrealms.plugin.api;

import com.runicrealms.plugin.player.stat.StatContainer;

import java.util.UUID;

public interface StatAPI {

    /**
     * @param uuid of the player
     * @return their dexterity stat
     */
    int getPlayerDexterity(UUID uuid);

    /**
     * @param uuid of the player
     * @return their intelligence stat
     */
    int getPlayerIntelligence(UUID uuid);

    /**
     * @param uuid of player to lookup
     * @return a wrapper containing the players combat stats (dex, int, etc.)
     */
    StatContainer getPlayerStatContainer(UUID uuid);

    /**
     * @param uuid of the player
     * @return their strength stat
     */
    int getPlayerStrength(UUID uuid);

    /**
     * @param uuid of the player
     * @return their vitality stat
     */
    int getPlayerVitality(UUID uuid);

    /**
     * @param uuid of the player
     * @return their wisdom stat
     */
    int getPlayerWisdom(UUID uuid);

    /**
     * Gets the value associated with the given stat
     *
     * @param uuid     of the player
     * @param statName the name of the stat "intelligence"
     * @return the value of that stat, or 0
     */
    int getStat(UUID uuid, String statName);

}
