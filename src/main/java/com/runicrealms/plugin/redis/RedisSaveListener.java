package com.runicrealms.plugin.redis;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.event.CacheSaveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RedisSaveListener implements Listener {

    private static final long EXPIRE_TIME = 86400; // seconds (24 hours)

    @EventHandler
    public void onCacheSave(CacheSaveEvent e) {

        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();

        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            // do all writing in here
            writePlayerDataToJedis(jedis, e.getPlayer());
        }
    }

    // todo: method to build player cache object from jedis
    // todo: method to write cache to jedis (stop storing objects in memory). get data from playerCache for now, but stop doing this
    public void writePlayerDataToJedis(Jedis jedis, Player player) {
        String uuid = String.valueOf(player.getUniqueId());
        Map<String, String> topLevelFields = new HashMap<String, String>() {{
            put("player_uuid", uuid);
            put("guild", "None");
            put("last_login", String.valueOf(LocalDate.now()));
        }};
        Map<String, String> character = new HashMap<String, String>() {{
            put("currentHp", String.valueOf(player.getHealth()));
        }};
        jedis.hmset(uuid, topLevelFields);
        jedis.hmset(uuid + ":character:3", character);
        jedis.expire(uuid, EXPIRE_TIME);
    }
}
