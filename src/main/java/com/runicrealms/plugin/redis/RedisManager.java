package com.runicrealms.plugin.redis;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.BaseCharacterInfo;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class to manager connection to redis and jedis pool
 */
public class RedisManager {

    private static final int REDIS_PORT = 17083; // TODO: env var
    private static final String REDIS_CONNECTION_STRING = "redis-17083.c15.us-east-1-2.ec2.cloud.redislabs.com"; // TODO: should be environment var
    public static final String REDIS_PASSWORD = "i3yIgvdVw13MbFO2RJF382dX8kvZzUaD"; // TODO: env var
    private final JedisPool jedisPool;

    public RedisManager() {

        jedisPool = new JedisPool(REDIS_CONNECTION_STRING, REDIS_PORT);

        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(REDIS_PASSWORD);
        }
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    /**
     * @param player
     * @return
     */
    public boolean checkRedisForPlayerData(Player player) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            if (jedis.exists(String.valueOf(player.getUniqueId()))) {
                Bukkit.broadcastMessage("redis data found");
                return true;
            }
        }
        Bukkit.broadcastMessage("redis data not found");
        return false;
    }

    /**
     * @param player
     * @param slot
     * @return
     */
    public boolean checkRedisForCharacterData(Player player, Integer slot) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            if (jedis.exists(player.getUniqueId() + ":character:" + slot)) {
//                CharacterData characterData = new CharacterData(player, slot, jedis);


                List<String> fields = new ArrayList<>();
                Map<String, String> fieldsMap = new HashMap<>();
                fields.addAll(BaseCharacterInfo.getFields());
                List<String> values = jedis.hmget(player.getUniqueId() + ":character:" + slot, fields.toArray(new String[0]));
                for (int i = 0; i < fields.toArray(new String[0]).length; i++) {
                    fieldsMap.put(fields.get(i), values.get(i));
                }
                for (String key : fieldsMap.keySet()) {
                    Bukkit.broadcastMessage("key is: " + key + ", and value is: " + fieldsMap.get(key));
                }


                Bukkit.broadcastMessage(ChatColor.GREEN + "redis character data found, building data from redis");
//                return new CharacterData(player, slot, jedis);
                return true;
            }
        }
        Bukkit.broadcastMessage(ChatColor.RED + "redis character data not found");
        return false;
    }
}
