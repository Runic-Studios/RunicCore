package com.runicrealms.plugin.redis;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.model.BaseCharacterData;
import com.runicrealms.plugin.model.CharacterData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.UUID;

/**
 * Singleton class to manager connection to redis and jedis pool
 */
public class RedisManager implements Listener {

    private static final int REDIS_PORT = 17083; // TODO: these should be env vars
    private static final String REDIS_CONNECTION_STRING = "redis-17083.c15.us-east-1-2.ec2.cloud.redislabs.com";
    public static final String REDIS_PASSWORD = "i3yIgvdVw13MbFO2RJF382dX8kvZzUaD";
    private final JedisPool jedisPool;

    public RedisManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(128); // max pool size
        jedisPool = new JedisPool(jedisPoolConfig, REDIS_CONNECTION_STRING, REDIS_PORT);
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(REDIS_PASSWORD);
        }
    }

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent e) {
        if (!updateBaseCharacterInfo(e.getPlayer(), e.getSlot()))
            Bukkit.getLogger().info(ChatColor.RED + "There was an error updating redis values on logout.");
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    /**
     * Checks redis to see if the currently selected character's data is cached.
     * And if it is, returns the CharacterData object
     *
     * @param uuid of player to check
     * @param slot of the character
     * @return a CharacterData object if it is found in redis
     */
    public CharacterData checkRedisForCharacterData(UUID uuid, Integer slot) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            String key = uuid + ":character:" + slot;
            if (jedis.exists(key)) {
                Bukkit.broadcastMessage(ChatColor.GREEN + "redis character data found, building data from redis");
                jedis.expire(key, RedisUtil.EXPIRE_TIME);
                return new CharacterData(uuid, slot, jedis);
            }
        }
        Bukkit.broadcastMessage(ChatColor.RED + "redis character data not found");
        return null;
    }

    /**
     * Saves basic character information on logout to redis session data
     *
     * @param player who is exiting the game
     * @param slot   of the current character
     * @return true if the data was successfully updated
     */
    public boolean updateBaseCharacterInfo(Player player, int slot) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
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
        }
        return false;
    }
}
