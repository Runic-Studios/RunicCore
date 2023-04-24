package com.runicrealms.plugin.api;

import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.UUID;

public interface RedisAPI {

    /**
     * Given a set of UUIDs in Redis, determines if the set contains the given slot
     *
     * @param redisDataSet a slot in Redis like "characterData"
     * @param slotToLoad   to check
     * @return true if there is data for the character slot
     */
    boolean determineIfDataInRedis(Set<String> redisDataSet, int slotToLoad);

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
     * Opens a new jedis resource which MUST BE CLOSED
     *
     * @return a jedis resource
     */
    Jedis getNewJedisResource();

    /**
     * @param uuid of player to check
     * @return a set of character slots (as strings) that have data in redis ending with the key
     */
    Set<String> getRedisDataSet(UUID uuid, String dataKey, Jedis jedis);

    /**
     * Removes the specified key and all sub-keys from redis
     *
     * @param jedis     the jedis resource
     * @param parentKey the parent key to remove (i.e., character, character:3, character:3:skills, etc.)
     */
    void removeAllFromRedis(Jedis jedis, String parentKey);

}
