package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoData;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * This interface is used for any data which is cached in redis during a play session
 *
 * @author Skyfallin
 */
public interface SessionData {

    /**
     * Ensures that all session data has a map of key-value string pairs for storage in jedis
     *
     * @return a map of key-value pairs
     */
    Map<String, String> toMap();

    /**
     * Ensures that all session data has a method to save the data in jedis
     *
     * @param jedis the jedis resource
     * @param slot  an optional argument to represent the character slot (for alt-specific data)
     */
    void writeToJedis(Jedis jedis, int... slot);

    /**
     * Ensure that all session data has a method to save the data in mongo
     *
     * @param playerMongoData the mongo data of the player
     * @param slot            an optional argument to represent the character slot (for alt-specific data)
     */
    void writeToMongo(PlayerMongoData playerMongoData, int... slot);
}
