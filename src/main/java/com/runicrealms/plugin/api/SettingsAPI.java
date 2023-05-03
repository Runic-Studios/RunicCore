package com.runicrealms.plugin.api;

import com.runicrealms.plugin.model.SettingsData;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public interface SettingsAPI {

    /**
     * Checks redis to see if the currently selected character's settings data is cached.
     * And if it is, returns the SettingsData object
     *
     * @param uuid  of player to check
     * @param jedis the jedis resource
     * @return a SettingsData object if it is found in redis
     */
    SettingsData checkRedisForSettingsData(UUID uuid, Jedis jedis);

    /**
     * Creates a SettingsData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     */
    SettingsData loadSettingsData(UUID uuid, Jedis jedis);

}
