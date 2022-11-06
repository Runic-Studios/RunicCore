package com.runicrealms.plugin.model;

import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.UUID;

public interface SessionDataManager {

    /**
     * @param uuid  of the player to lookup
     * @param jedis the jedis resource
     * @return a SessionData object if it is found in jedis, else null
     */
    SessionData checkJedisForSessionData(UUID uuid, Jedis jedis);

    /**
     * @return a map of uuid to their session data (for in-memory caching)
     */
    Map<UUID, SessionData> getSessionDataMap();

    /**
     * Attempts to load the session data for player from memory if it is found
     *
     * @param uuid of the player
     * @return the session data associated with this uuid
     */
    SessionData loadSessionData(UUID uuid);

    /**
     * Loads session data for player from jedis
     *
     * @param uuid  of the player
     * @param jedis the jedis resource
     * @return the session data associated with this uuid
     */
    SessionData loadSessionData(UUID uuid, Jedis jedis);
}
