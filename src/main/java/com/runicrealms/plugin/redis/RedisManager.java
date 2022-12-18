package com.runicrealms.plugin.redis;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RedisAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.model.BaseCharacterData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Singleton class to manager connection to redis and jedis pool
 */
public class RedisManager implements Listener, RedisAPI {

    public static final String REDIS_PASSWORD = "i3yIgvdVw13MbFO2RJF382dX8kvZzUaD";
    private static final int MAX_CONNECTIONS = 128;
    private static final int REDIS_PORT = 17083; // TODO: these should be env vars (application.properties)
    private static final String REDIS_CONNECTION_STRING = "redis-17083.c15.us-east-1-2.ec2.cloud.redislabs.com";
    private final JedisPool jedisPool;

    public RedisManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(MAX_CONNECTIONS); // max pool size
        jedisPoolConfig.setMaxIdle(MAX_CONNECTIONS);
        jedisPool = new JedisPool(jedisPoolConfig, REDIS_CONNECTION_STRING, REDIS_PORT);
    }

    @Override
    public String getCharacterKey(UUID uuid, int slot) {
        return uuid + ":character:" + slot;
    }

    @Override
    public long getExpireTime() {
        return 86400; // seconds (24 hours)
    }

    @Override
    public List<String> getNestedKeys(String parentKey, Jedis jedis) {
        List<String> nestedKeys = new ArrayList<>();
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
        /*
        String list is in format 'uuid:character:achievements'
        This code makes it return 'achievements', or the final nested key, which is intended behavior
         */
        for (String nestedKey : nestedKeys) {
            String[] split = nestedKey.split(":");
            nestedKeys.set(nestedKeys.indexOf(nestedKey), split[split.length - 1]);
        }
        return nestedKeys;
    }

    @Override
    public Jedis getNewJedisResource() {
        Jedis jedis = jedisPool.getResource();
        jedis.auth(RedisManager.REDIS_PASSWORD);
        return jedis;
    }

    @Override
    public void removeAllFromRedis(Jedis jedis, String parentKey) {
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

    @Override
    public boolean updateBaseCharacterInfo(Player player, int slot, Jedis jedis) {
        if (jedis.exists(player.getUniqueId() + ":character:" + slot)) {
            String key = player.getUniqueId() + ":character:" + slot;
            BaseCharacterData baseCharacterData = new BaseCharacterData
                    (
                            slot,
                            (int) player.getHealth(),
                            player.getFoodLevel(),
                            player.getUniqueId(),
                            player.getLocation()
                    );
            jedis.hmset(key, baseCharacterData.toMap());
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs last
    public void onCharacterQuit(CharacterQuitEvent event) {
        if (!updateBaseCharacterInfo(event.getPlayer(), event.getSlot(), event.getJedis())) // todo: this ain't right
            Bukkit.getLogger().info(ChatColor.RED + "There was an error updating redis values on logout.");
    }
}
