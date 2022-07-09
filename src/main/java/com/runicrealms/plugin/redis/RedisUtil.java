package com.runicrealms.plugin.redis;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisUtil {

    /**
     * @param player
     * @param field
     * @return
     */
    public static String getRedisValue(Player player, String field) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD); // not sure if we need to do this every time
            int slot = RunicCore.getDatabaseManager().getLoadedCharactersMap().get(player.getUniqueId());
            String key = player.getUniqueId() + ":character:" + slot;
            if (jedis.exists(key)) {
                return jedis.hmget(key, field).get(0);
            }
        }
        return "";
    }

    /**
     * @param player
     * @param fields
     * @return
     */
    public static Map<String, String> getRedisValues(Player player, List<String> fields) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD); // not sure if we need to do this every time
            int slot = RunicCore.getDatabaseManager().getLoadedCharactersMap().get(player.getUniqueId());
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
        }
        return new HashMap<>();
    }

    /**
     * @param player
     * @param field
     * @param value
     * @return
     */
    public static boolean setRedisValue(Player player, String field, String value) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD); // not sure if we need to do this every time
            int slot = RunicCore.getDatabaseManager().getLoadedCharactersMap().get(player.getUniqueId());
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
}
