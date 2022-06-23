package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.gui.CharacterInfo;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.MongoData;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This intermediary object acts as an easy way to pass data from mongo to memory to redis, or vice versa
 * It is used during login to both display data in the character select screen and load basic info for the player
 * on character select, then it is removed after.
 *
 * @author Skyfallin
 */
public class PlayerData {

    private static final long JEDIS_EXPIRE_TIME = 86400; // seconds (24 hours)
    private static final String DATA_SECTION_KEY = "character";
    private final UUID playerUuid;
    private final String guild;
    private final Map<Integer, CharacterInfo> playerCharacters;

    /**
     * Build basic info about the player for the character select screen from mongo
     *
     * @param player    to be loaded
     * @param mongoData associated with the player's unique id
     */
    public PlayerData(Player player, MongoData mongoData) {
        this.playerUuid = player.getUniqueId();
        this.guild = mongoData.get("guild", String.class);
        this.playerCharacters = new HashMap<>();
        try {
            if (mongoData.has(DATA_SECTION_KEY)) {
                for (String key : mongoData.getSection(DATA_SECTION_KEY).getKeys()) {
                    playerCharacters.put(Integer.parseInt(key), new CharacterInfo(
                            ClassEnum.getFromName(mongoData.get(DATA_SECTION_KEY + "." + key + ".class.name", String.class)),
                            mongoData.get(DATA_SECTION_KEY + "." + key + ".class.exp", Integer.class),
                            mongoData.get(DATA_SECTION_KEY + "." + key + ".class.level", Integer.class)));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            writePlayerDataToJedis(jedis);
        }
    }

    /**
     * Sends the information in this object to redis for caching
     *
     * @param jedis a resource from the jedis pool
     */
    public void writePlayerDataToJedis(Jedis jedis) {
        String uuid = String.valueOf(this.playerUuid);
        Map<String, String> topLevelFields = new HashMap<String, String>() {{
            put("player_uuid", uuid);
            put("guild", guild);
            put("last_login", String.valueOf(LocalDate.now()));
        }};
        jedis.hmset(uuid, topLevelFields);
        for (Integer characterSlot : playerCharacters.keySet()) {
            jedis.hmset(uuid + ":character:" + characterSlot + ":class", characterClassData(playerCharacters.get(characterSlot)));
        }
        jedis.expire(uuid, JEDIS_EXPIRE_TIME);
    }

    /**
     * Map helper to place nested strings in jedis
     *
     * @param characterInfo the basic character info used for display in the select screen
     * @return a map of strings that can be nested in jedis
     */
    private Map<String, String> characterClassData(CharacterInfo characterInfo) {
        return new HashMap<String, String>() {{
            put("exp", String.valueOf(characterInfo.getExp()));
            put("level", String.valueOf(characterInfo.getLevel()));
            put("name", characterInfo.getClassType().getName());
        }};
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getGuild() {
        return guild;
    }

    public Map<Integer, CharacterInfo> getPlayerCharacters() {
        return playerCharacters;
    }
}
