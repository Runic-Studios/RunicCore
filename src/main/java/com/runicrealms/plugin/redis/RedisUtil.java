package com.runicrealms.plugin.redis;

import com.runicrealms.plugin.api.RunicCoreAPI;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;

public class RedisUtil {

    public static final long EXPIRE_TIME = 86400; // seconds (24 hours)

    /**
     * Removes all character-specific data from the redis cache
     *
     * @param jedis the jedis resource
     * @param key   to remove
     */
    public static void removeFromRedis(Jedis jedis, String key) {
        if (jedis.exists(key))
            jedis.del(key);
    }

    /**
     * Removes the specified key and all sub-keys from redis
     *
     * @param jedis     the jedis resource
     * @param parentKey the parent key to remove (i.e., character, character:3, character:3:skills, etc.)
     */
    public static void removeAllFromRedis(Jedis jedis, String parentKey) {
        String subKeyPath = parentKey + ":*"; // grab all sub-keys
        ScanParams scanParams = new ScanParams().count(100).match(subKeyPath);
        String cur = ScanParams.SCAN_POINTER_START;
        boolean cycleIsFinished = false;
        while (!cycleIsFinished) {
            ScanResult<String> scanResult = jedis.scan(cur, scanParams);
            scanResult.getResult().forEach(jedis::del);
            cur = scanResult.getCursor();
            if (cur.equals("0")) {
                cycleIsFinished = true;
            }
        }
        jedis.del(parentKey);
    }

    /**
     * Returns all the keys nested from the parent key.
     * Useful for cascading deletion or nested value iteration
     * Can also use hgetAll if the nested keys match a single pattern
     *
     * @param jedisPool the jedis pool from core
     * @param parentKey the parent key to remove (i.e., character:3)
     * @return a list of redis keys nested inside the parent
     */
    public static List<String> getNestedKeys(JedisPool jedisPool, String parentKey) {
        List<String> nestedKeys = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            String subKeyPath = parentKey + ":*"; // grab all sub-keys
            ScanParams scanParams = new ScanParams().count(100).match(subKeyPath);
            String cur = ScanParams.SCAN_POINTER_START;
            boolean cycleIsFinished = false;
            while (!cycleIsFinished) {
                ScanResult<String> scanResult = jedis.scan(cur, scanParams);
                nestedKeys.addAll(scanResult.getResult());
                cur = scanResult.getCursor();
                if (cur.equals("0")) {
                    cycleIsFinished = true;
                }
            }
        }
        return nestedKeys;
    }

    /**
     * Opens a jedis resource, authenticates it, reads and returns a value, and closes the connection
     * Used for account-wide fields
     *
     * @param uuid  of the player to lookup in redis
     * @param field the field to lookup (it's key-value pairs)
     * @return the value corresponding to the field
     */
    public static String getRedisValue(UUID uuid, String field, Jedis jedis) {
        String key = uuid.toString();
        if (jedis.exists(key)) {
            return jedis.hmget(key, field).get(0);
        }
        return "";
    }

    /**
     * Opens a jedis resource, authenticates it, reads and returns a value, and closes the connection
     * Used for character-specific lookups
     *
     * @param uuid  of the player to lookup in redis
     * @param field the field to lookup (it's key-value pairs)
     * @param slot  of the selected character
     * @return the value corresponding to the field
     */
    public static String getRedisValue(UUID uuid, String field, int slot, Jedis jedis) {
        String key = uuid + ":character:" + slot;
        if (jedis.exists(key)) {
            return jedis.hmget(key, field).get(0);
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
    public static Map<String, String> getRedisValues(Player player, List<String> fields, Jedis jedis) {
        int slot = RunicCoreAPI.getCharacterSlot(player.getUniqueId());
        String key = player.getUniqueId() + ":character:" + slot;
        if (jedis.exists(key)) {
            Map<String, String> fieldsMap = new HashMap<>();
            String[] fieldsToArray = fields.toArray(new String[0]);
            List<String> values = jedis.hmget(key, fieldsToArray);
            for (int i = 0; i < fieldsToArray.length; i++) {
                fieldsMap.put(fieldsToArray[i], values.get(i));
            }
            return fieldsMap;
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
    public static boolean setRedisValue(Player player, String field, String value, Jedis jedis) {
        int slot = RunicCoreAPI.getCharacterSlot(player.getUniqueId());
        String key = player.getUniqueId() + ":character:" + slot;
        if (jedis.exists(key)) {
            Map<String, String> fieldsMap = new HashMap<String, String>() {{
                put(field, value);
            }};
            jedis.hmset(key, fieldsMap);
            return true;
        }
        return false;
    }

    /**
     * Attempts to update the nested redis value corresponding to the field for the given key
     *
     * @param key   the path of the field in redis
     * @param field of the value (e.g., "currentHp")
     * @param value to write to the field
     * @return true if the field was successfully written to
     */
    public static boolean setRedisValue(String key, String field, String value, Jedis jedis) {
        if (jedis.exists(key)) {
            Map<String, String> fieldsMap = new HashMap<String, String>() {{
                put(field, value);
            }};
            jedis.hmset(key, fieldsMap);
            return true;
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
    public static boolean setRedisValues(Player player, Map<String, String> map, Jedis jedis) {
        int slot = RunicCoreAPI.getCharacterSlot(player.getUniqueId());
        String key = player.getUniqueId() + ":character:" + slot;
        if (jedis.exists(key)) {
            jedis.hmset(key, map);
            return true;
        }
        return false;
    }
}
