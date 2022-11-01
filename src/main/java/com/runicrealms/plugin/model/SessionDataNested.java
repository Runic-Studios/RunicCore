package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.PlayerMongoData;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * This interface is a slightly different variant of SessionData for data modeling that is embedded deeply in jedis
 * Current plugin uses: items, achievements, quests
 *
 * @author Skyfallin
 */
public interface SessionDataNested {

    /**
     * Different implementation of toMap that allows for a nested object
     *
     * @param nestedObject
     * @return a map of key-value pairs of data on the nested object
     */
    Map<String, String> toMap(Object nestedObject);

    /**
     * Method to return some data from jedis as a map of key-value pairs
     *
     * @param jedis        the jedis resource
     * @param nestedObject some nested object to get the data map for
     * @param slot         an optional character slot for character-specific data
     * @return a map of key-value pairs
     */
    Map<String, String> getDataMapFromJedis(Jedis jedis, Object nestedObject, int... slot);

    /**
     * Ensures that all session data has a method to save the data in jedis
     *
     * @param jedis the jedis resource
     * @param slot  an optional argument to represent the character slot (for alt-specific data)
     */
    void writeToJedis(Jedis jedis, int... slot);

    /**
     * Ensure that all session data has a method to save the data in mongo.
     * Saves from jedis, which is available even if player is offline
     *
     * @param playerMongoData the mongo data of the player
     * @param jedis           the jedis resource
     * @param slot            an optional argument to represent the character slot (for alt-specific data)
     */
    PlayerMongoData writeToMongo(PlayerMongoData playerMongoData, Jedis jedis, int... slot);
}
