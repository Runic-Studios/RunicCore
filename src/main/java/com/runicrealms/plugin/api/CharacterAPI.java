package com.runicrealms.plugin.api;

import com.runicrealms.plugin.classes.CharacterClass;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface CharacterAPI {

    /**
     * Returns the currently selected character for the given player
     *
     * @param uuid of the player
     * @return an int representing their character slot (3, for example)
     */
    int getCharacterSlot(UUID uuid);

    /**
     * Gets a key set view of all characters which are loaded in the game. (Excludes players in lobby and black box)
     *
     * @return a map of uuid to a pair of the character's slot and the character's class
     */
    ConcurrentHashMap.KeySetView<UUID, Pair<Integer, CharacterClass>> getLoadedCharacters();

    /**
     * Quick method to grab player class from session data in redis
     *
     * @param player to lookup
     * @return a string representing the class (Cleric, Mage, etc.)
     */
    String getPlayerClass(Player player);

    /**
     * Quick method to grab player class from cached in-memory data
     *
     * @param uuid of player to lookup
     * @return a string representing the class (Cleric, Mage, etc.)
     */
    String getPlayerClass(UUID uuid);

    /**
     * Method to grab player class from redis, useful if player is offline
     *
     * @param uuid  of the player to check
     * @param slot  of the character
     * @param jedis the jedis resource
     * @return a string representing the class (Cleric, Mage, etc.)
     */
    String getPlayerClass(UUID uuid, int slot, Jedis jedis);

    /**
     * @param player
     * @return
     */
    boolean hasSelectedCharacter(Player player);


}
