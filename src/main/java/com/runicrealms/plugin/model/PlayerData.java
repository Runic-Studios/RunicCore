package com.runicrealms.plugin.model;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.database.MongoData;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

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

    private static final String DATA_SECTION_KEY = "character";
    private final UUID playerUuid;
    private final String guild;
    private final Map<Integer, ClassData> playerCharacters;

    /**
     * Build basic info about the player for the character select screen from mongo
     *
     * @param player    to be loaded
     * @param mongoData associated with the player's unique id
     */
    public PlayerData(Player player, MongoData mongoData, Jedis jedis) {
        this.playerUuid = player.getUniqueId();
        this.guild = mongoData.get("guild", String.class);
        this.playerCharacters = new HashMap<>();
        try {
            if (mongoData.has(DATA_SECTION_KEY)) {
                for (String key : mongoData.getSection(DATA_SECTION_KEY).getKeys()) {
                    playerCharacters.put(Integer.parseInt(key), new ClassData(
                            playerUuid,
                            CharacterClass.getFromName(mongoData.get(DATA_SECTION_KEY + "." + key + ".class.name", String.class)),
                            mongoData.get(DATA_SECTION_KEY + "." + key + ".class.level", Integer.class),
                            mongoData.get(DATA_SECTION_KEY + "." + key + ".class.exp", Integer.class)));
                    updateFromRedis(this.playerUuid, Integer.parseInt(key), jedis);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param character
     */
    public void addCharacter(ClassData character) {
        this.playerCharacters.put(findFirstUnusedSlot(), character);
    }

    /**
     * Removes a player character slot from the in-memory cache
     *
     * @param slot of the character
     */
    public void removeCharacter(Integer slot) {
        this.playerCharacters.remove(slot);
        this.findFirstUnusedSlot();
    }

    /**
     * Determines which character slot (1-10) is the first unused for our player.
     * Used when making a new character
     */
    public int findFirstUnusedSlot() {
        for (int i = 1; i <= 10; i++) {
            if (this.playerCharacters.get(i) == null) {
                return i;
            }
        }
        return 1;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getGuild() {
        return guild;
    }

    public Map<Integer, ClassData> getPlayerCharacters() {
        return playerCharacters;
    }

    /**
     * Updates the memoized list of player characters for display in the character select menu.
     * Used on logout and whenever the class data might change in-game (levels, etc.)
     * Designed this way because if they're only playing one character over and over, no need to load everything into redis.
     * We can just read whatever they have already loaded. Idk. Maybe this object should be cached.
     *
     * @param uuid of the player
     * @param slot of the character
     */
    private boolean updateFromRedis(UUID uuid, int slot, Jedis jedis) {
        if (jedis.exists(uuid + ":character:" + slot)) {
            ClassData classData = new ClassData(uuid, slot, jedis);
            this.playerCharacters.put(slot, classData);
            return true;
        } else {
            // log error
        }
        return false;
    }
}
