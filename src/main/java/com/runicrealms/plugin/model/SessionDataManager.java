package com.runicrealms.plugin.model;

import redis.clients.jedis.Jedis;

public interface SessionDataManager {

    /**
     * @param identifier of the session data. uuid for player, or prefix for guild
     * @param jedis      the jedis resource
     * @param slot       an optional parameter for the character slot
     * @return a SessionDataRedis object if it is found in jedis, else null
     */
    SessionDataRedis checkRedisForSessionData(Object identifier, Jedis jedis, int... slot);

    /**
     * Some objects are stored in-memory for fast lookups
     * This method can be overridden to return the in-memory data structure for the object
     *
     * @param identifier of the session data. uuid for player, or prefix for guild
     * @param slot       an optional parameter for the character slot
     * @return null, or the in-memory object if it is used
     */
    default SessionDataRedis getSessionData(Object identifier, int... slot) {
        return null;
    }

    /**
     * Loads session data for player from Redis
     *
     * @param identifier of the session data. uuid for player, or prefix for guild
     * @param jedis      the jedis resource
     * @param slot       an optional parameter for the character slot
     * @return the session data associated with this uuid
     */
    SessionDataRedis loadSessionData(Object identifier, Jedis jedis, int... slot);
}
