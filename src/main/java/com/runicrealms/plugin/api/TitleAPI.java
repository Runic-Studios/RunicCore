package com.runicrealms.plugin.api;

import com.runicrealms.plugin.model.TitleData;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public interface TitleAPI {

    /**
     * Checks redis to see if the currently selected character's title data is cached.
     * And if it is, returns the Title object
     *
     * @param uuid  of player to check
     * @param jedis the jedis resource
     * @return a TitleData object if it is found in redis
     */
    TitleData checkRedisForTitleData(UUID uuid, Jedis jedis);

    /**
     * Tries to retrieve a TitleData object from server memory
     *
     * @param uuid of the player
     * @return a TitleData object
     */
    TitleData getTitleData(UUID uuid);

    /**
     * Creates a TitleData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     */
    TitleData loadTitleData(UUID uuid, Jedis jedis);

    /**
     * Removes all prefixes AND suffixes for the given player
     *
     * @param player        remove titles for
     * @param writeCallback function to execute on completion
     */
    void removePrefixesAndSuffixes(Player player, WriteCallback writeCallback);

}
