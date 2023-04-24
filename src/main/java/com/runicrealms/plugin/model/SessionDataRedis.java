package com.runicrealms.plugin.model;

import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This interface is used for any data which is cached in redis during a play session
 *
 * @author Skyfallin
 */
public interface SessionDataRedis {

    /**
     * Method to return some data from jedis as a map of key-value pairs
     *
     * @param uuid of the player
     * @return a map of key-value pairs
     */
    Map<String, String> getDataMapFromJedis(UUID uuid, Jedis jedis, int... slot);

    /**
     * @return a list of jedis fields
     */
    List<String> getFields();

    /**
     * Ensures that all session data has a map of key-value string pairs for storage in jedis
     *
     * @param uuid of the player
     * @return a map of key-value pairs
     */
    Map<String, String> toMap(UUID uuid, int... slot);

    /**
     * Ensures that all session data has a method to save the data in jedis ASYNC
     *
     * @param uuid  of the player (used to key most values)
     * @param jedis the jedis resource
     * @param slot  an optional argument to represent the character slot (for alt-specific data)
     */
    void writeToJedis(UUID uuid, Jedis jedis, int... slot);

}
