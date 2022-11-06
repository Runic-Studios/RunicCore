package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.MongoData;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

/**
 * This interface is used for any data which is cached in redis during a play session
 *
 * @author Skyfallin
 */
public interface SessionData {

    /**
     * Method to return some data from jedis as a map of key-value pairs
     *
     * @return a map of key-value pairs
     */
    Map<String, String> getDataMapFromJedis(Jedis jedis, int... slot);

    /**
     * @return a list of jedis fields
     */
    List<String> getFields();

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
     * @param mongoData the mongo data of the player or guild
     * @param slot      an optional argument to represent the character slot (for alt-specific data)
     */
    MongoData writeToMongo(MongoData mongoData, int... slot);
}
