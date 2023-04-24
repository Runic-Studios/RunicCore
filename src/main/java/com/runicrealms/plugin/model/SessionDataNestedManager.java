package com.runicrealms.plugin.model;

import redis.clients.jedis.Jedis;

import java.util.Map;

public interface SessionDataNestedManager {

    /**
     * @param identifier of the session data. uuid for player, or prefix for guild
     * @param jedis      the jedis resource
     * @param slot       an optional parameter for the character slot
     * @return a SessionDataNested object if it is found in jedis, else null
     */
    SessionDataNested checkJedisForSessionData(Object identifier, Jedis jedis, int... slot);

    /**
     * Attempts to load the session data for player from memory if it is found
     *
     * @param identifier of the session data. uuid for player, or prefix for guild
     * @param slot       an optional parameter for the character slot
     * @return the session data associated with this uuid
     */
    SessionDataNested getSessionData(Object identifier, int... slot);

    /**
     * @return a map of identifier (uuid or prefix) to their session data (for in-memory caching)
     */
    Map<Object, SessionDataNested> getSessionDataMap();

    /**
     * Loads session data for player from jedis
     *
     * @param identifier of the session data. uuid for player, or prefix for guild
     * @param slot       an optional parameter for the character slot
     * @return the session data associated with this uuid
     */
    SessionDataNested loadSessionData(Object identifier, int... slot);
}
