package com.runicrealms.plugin.redis;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RedisSaveListener implements Listener {

    private static final long EXPIRE_TIME = 86400; // seconds (24 hours)

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onCacheSave(CharacterQuitEvent e) {

//        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
//
//        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
//            // do all writing in here
//            writePlayerDataToJedis(jedis, e.getPlayer());
//        }
    }

    // todo: method to build player cache object from jedis
    // todo: method to write cache to jedis (stop storing objects in memory). get data from playerCache for now, but stop doing this
    // TODO: can remove many fields from the playercache. should change to playerData and trim down fields

    /**
     * @param jedis
     * @param player
     */
    public void writePlayerDataToJedis(Jedis jedis, Player player) {
        String uuid = String.valueOf(player.getUniqueId());
        Map<String, String> topLevelFields = new HashMap<String, String>() {{
            put("player_uuid", uuid);
            put("guild", "None");
            put("last_login", String.valueOf(LocalDate.now()));
        }};
        jedis.hmset(uuid, topLevelFields);
        String characterSlot = String.valueOf(RunicCoreAPI.getPlayerCache(player).getCharacterSlot());
        jedis.hmset(uuid + ":character:" + characterSlot, playerCharacterData(player));
        jedis.hmset(uuid + ":character:" + characterSlot + ":class", characterClassData(player));
        jedis.expire(uuid, EXPIRE_TIME);
    }

    /**
     * @param player
     * @return
     */
    private Map<String, String> playerCharacterData(Player player) {
        return new HashMap<String, String>() {{
            put("currentHp", String.valueOf(player.getHealth()));
            put("maxMana", String.valueOf(RunicCoreAPI.getPlayerCache(player).getMaxMana()));
        }};
    }

    /**
     * @param player
     * @return
     */
    private Map<String, String> characterClassData(Player player) {
        return new HashMap<String, String>() {{
            put("exp", String.valueOf(RunicCoreAPI.getPlayerCache(player).getClassExp()));
            put("level", String.valueOf(player.getLevel()));
            put("name", RunicCoreAPI.getPlayerCache(player).getClassName());
        }};
    }
}
