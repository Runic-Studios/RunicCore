package com.runicrealms.plugin.api;

import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

public interface RedisAPI {

    /**
     * Characters have a character-specific section of redis. This returns the parent key for the section
     *
     * @param uuid of the player
     * @param slot of the character
     * @return the parent key of character's section
     */
    String getCharacterKey(UUID uuid, int slot);

    /**
     * @return the time a redis key should expire
     */
    long getExpireTime();

    /**
     * Returns a string list of all nested keys in jedis beginning with the parent key
     * Useful for cascading deletion or nested value iteration
     * Can also use hgetAll if the nested keys match a single pattern
     *
     * @param parentKey the key at the top of the section (i.e., character:3)
     * @param jedis     the jedis resource
     * @return a list of nested keys
     */
    List<String> getNestedKeys(String parentKey, Jedis jedis);

    /**
     * Opens a new jedis resource which MUST BE CLOSED
     *
     * @return a jedis resource
     */
    Jedis getNewJedisResource();

    /**
     * Removes the specified key and all sub-keys from redis
     *
     * @param jedis     the jedis resource
     * @param parentKey the parent key to remove (i.e., character, character:3, character:3:skills, etc.)
     */
    void removeAllFromRedis(Jedis jedis, String parentKey);

    /**
     * Saves basic character information on logout to redis session data
     *
     * @param player who is exiting the game
     * @param slot   of the current character
     * @param jedis  the jedis resource (from character select or quit event)
     * @return true if the data was successfully updated
     */
    boolean updateBaseCharacterInfo(Player player, int slot, Jedis jedis);

}
