package com.runicrealms.plugin.redis;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RedisAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Singleton class to manager connection to redis and jedis pool
 */
public class RedisManager implements Listener, RedisAPI {
    public static final String REDIS_PASSWORD = "i3yIgvdVw13MbFO2RJF382dX8kvZzUaD";
    private static final int MAX_CONNECTIONS = 128;
    private static final int REDIS_PORT = 17083; // TODO: these should be in a local config file on the machine
    private static final int TIMEOUT = 5000; // 5 seconds
    private static final String REDIS_CONNECTION_STRING = "redis-17083.c15.us-east-1-2.ec2.cloud.redislabs.com";
    private final JedisPool jedisPool;

    public RedisManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(MAX_CONNECTIONS); // max pool size
        jedisPoolConfig.setMaxIdle(MAX_CONNECTIONS);
        jedisPoolConfig.setMinIdle(15);
        // Create our authenticated jedis pool
        jedisPool = new JedisPool(jedisPoolConfig, REDIS_CONNECTION_STRING, REDIS_PORT, TIMEOUT, REDIS_PASSWORD);
    }

    @Override
    public boolean determineIfDataInRedis(Set<String> redisDataSet, int slotToLoad) {
        if (slotToLoad != -1) {
            return redisDataSet.contains(String.valueOf(slotToLoad));
        } else {
            for (int i = 1; i <= RunicCore.getDataAPI().getMaxCharacterSlot(); i++) {
                if (redisDataSet.contains(String.valueOf(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getCharacterKey(UUID uuid, int slot) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        return database + ":" + uuid + ":character:" + slot;
    }

    @Override
    public long getExpireTime() {
        // 2419200 (28 days, or roughly four weeks)
        return 1209600; // seconds (two weeks)
    }

    @Override
    public Jedis getNewJedisResource() {
        Jedis jedis = jedisPool.getResource();
        jedis.auth(RedisManager.REDIS_PASSWORD);
        return jedis;
    }

    @Override
    public Set<String> getRedisDataSet(UUID uuid, String dataKey, Jedis jedis) {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        return jedis.smembers(database + ":" + uuid + ":" + dataKey);
    }

    @Override
    public void removeAllFromRedis(Jedis jedis, String parentKey) {
        try {
            ScanParams scanParams = new ScanParams().match(parentKey + ":*").count(100);
            String cursor = ScanParams.SCAN_POINTER_START;
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                List<String> keys = scanResult.getResult();
                for (String key : keys) {
                    jedis.del(key);
                }
                cursor = scanResult.getCursor();
            } while (!cursor.equals("0"));
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "removeAllFromRedis() failed");
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs last
    public void onCharacterQuit(CharacterQuitEvent event) {
//        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
//            event.getCoreCharacterData().writeToJedis(event.getPlayer().getUniqueId(), jedis, event.getSlot());
//        }
    }
}
