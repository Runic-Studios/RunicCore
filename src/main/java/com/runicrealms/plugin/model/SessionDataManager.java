package com.runicrealms.plugin.model;

import redis.clients.jedis.Jedis;

import java.util.Map;

public interface SessionDataManager {

    /**
     * @param identifier of the session data. uuid for player, or prefix for guild
     * @param jedis      the jedis resource
     * @return a SessionData object if it is found in jedis, else null
     */
    SessionData checkJedisForSessionData(Object identifier, Jedis jedis);

    /**
     * @return a map of identifier (uuid or prefix) to their session data (for in-memory caching)
     */
    Map<Object, SessionData> getSessionDataMap();

    /**
     * Attempts to load the session data for player from memory if it is found
     *
     * @param identifier of the session data. uuid for player, or prefix for guild
     * @return the session data associated with this uuid
     */
    SessionData loadSessionData(Object identifier);

    /**
     * Loads session data for player from jedis
     *
     * @param identifier of the session data. uuid for player, or prefix for guild
     * @param jedis      the jedis resource
     * @return the session data associated with this uuid
     */
    SessionData loadSessionData(Object identifier, Jedis jedis);
}
