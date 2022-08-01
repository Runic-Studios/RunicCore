package com.runicrealms.plugin.redis;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisUtil {

    /**
     * Opens a jedis resource, authenticates it, reads and returns a value, and closes the connection
     *
     * @param player the player to lookup in redis
     * @param field  the field to lookup (it's key-value pairs)
     * @return the value corresponding to the field
     */
    public static String getRedisValue(Player player, String field) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            int slot = RunicCoreAPI.getCharacterSlot(player.getUniqueId());
            String key = player.getUniqueId() + ":character:" + slot;
            if (jedis.exists(key)) {
                return jedis.hmget(key, field).get(0);
            }
        }
        return "";
    }

    /**
     * Opens a jedis resource, authenticates it, reads and returns a map of key-value pairs, and closes the connection
     *
     * @param player the player to lookup in redis
     * @param fields the fields to lookup (it's key-value pairs, returned in a map)
     * @return the values corresponding to the field
     */
    public static Map<RedisField, String> getRedisValues(Player player, List<RedisField> fields) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            int slot = RunicCoreAPI.getCharacterSlot(player.getUniqueId());
            String key = player.getUniqueId() + ":character:" + slot;
            if (jedis.exists(key)) {
                Map<RedisField, String> fieldsMap = new HashMap<>();
                List<String> fieldsToString = redisFieldsToStrings(fields);
                String[] fieldsToArray = fieldsToString.toArray(new String[0]);
                List<String> values = jedis.hmget(key, fieldsToArray);
                for (int i = 0; i < fieldsToArray.length; i++) {
                    fieldsMap.put(RedisField.getFromFieldString(fieldsToArray[i]), values.get(i));
                }
                return fieldsMap;
            }
        }
        return new HashMap<>();
    }

    /**
     * Attempts to update the redis value corresponding to the field for the given player
     *
     * @param player to write value for
     * @param field  of the value (e.g., "currentHp")
     * @param value  to write to the field
     * @return true if the field was successfully written to
     */
    public static boolean setRedisValue(Player player, String field, String value) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            int slot = RunicCoreAPI.getCharacterSlot(player.getUniqueId());
            String key = player.getUniqueId() + ":character:" + slot;
            if (jedis.exists(key)) {
                Map<String, String> fieldsMap = new HashMap<String, String>() {{
                    put(field, value);
                }};
                jedis.hmset(key, fieldsMap);
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to update the redis value corresponding to the field for the given player
     *
     * @param player to write value for
     * @param map    of field, value to write
     * @return true if the field was successfully written to
     */
    public static boolean setRedisValues(Player player, Map<String, String> map) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            int slot = RunicCoreAPI.getCharacterSlot(player.getUniqueId());
            String key = player.getUniqueId() + ":character:" + slot;
            if (jedis.exists(key)) {
                jedis.hmset(key, map);
                return true;
            }
        }
        return false;
    }

    /**
     * Quick conversion method to grab the strings from a list of redis fields
     *
     * @param redisFields a list of redis field constants
     * @return a list of strings that can be placed in the session cache
     */
    public static List<String> redisFieldsToStrings(List<RedisField> redisFields) {
        List<String> fieldsToString = new ArrayList<>();
        for (RedisField redisField : redisFields) {
            fieldsToString.add(redisField.getField());
        }
        return fieldsToString;
    }
}
