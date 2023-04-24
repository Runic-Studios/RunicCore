package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import org.bukkit.entity.Player;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
public class ProjectedData {
    private final UUID uuid;
    private final Map<Integer, ClassData> playerCharacters;

    public ProjectedData(Player player, Jedis jedis) {
        this.uuid = player.getUniqueId();
        this.playerCharacters = new HashMap<>();
        try {
            for (int i = 1; i <= RunicCore.getDataAPI().getMaxCharacterSlot(); i++) {
                // Try to project character data from redis
                boolean foundInRedis = false;
//                boolean foundInRedis = updateFromRedis(this.uuid, i, jedis); // todo: add redis logic back
//                Bukkit.getLogger().warning("found in redis char " + i + " was " + foundInRedis);
                // Try to project from mongo
                if (!foundInRedis) {
                    ClassData result = loadClassData(i);
                    // todo: add to redis
                    if (result != null) {
//                        Bukkit.getLogger().warning("loading class data for char " + i + " was not null");
                        playerCharacters.put(i, result);
                    } else {
//                        Bukkit.getLogger().warning("loading class data for char " + i + " was null!!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param character the Data Transfer Object
     */
    public void addCharacter(ClassData character) {
        this.playerCharacters.put(findFirstUnusedSlot(), character);
    }

    /**
     * Determines which character slot (1-10) is the first unused for our player.
     * Used when making a new character
     */
    public int findFirstUnusedSlot() {
        for (int i = 1; i <= RunicCore.getDataAPI().getMaxCharacterSlot(); i++) {
            if (this.playerCharacters.get(i) == null) {
                return i;
            }
        }
        return 1;
    }

    public Map<Integer, ClassData> getPlayerCharacters() {
        return playerCharacters;
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * This projects the fields for the class type, level, and experience so that we can display them
     * in the select screen with faster lookup times
     *
     * @param slot of the character to project
     * @return a ClassData object wrapper with projected fields
     */
    private ClassData loadClassData(int slot) {
        // Find our top-level document
        Query query = new Query(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(this.uuid));
        // Project only the fields we need
        query.fields()
                .include("coreCharacterDataMap." + slot + "." + CharacterField.CLASS_TYPE.getField())
                .include("coreCharacterDataMap." + slot + "." + CharacterField.CLASS_LEVEL.getField())
                .include("coreCharacterDataMap." + slot + "." + CharacterField.CLASS_EXP.getField());
        CorePlayerData corePlayerData = RunicCore.getDataAPI().getMongoTemplate().findOne(query, CorePlayerData.class);
        if (corePlayerData == null) return null;
        if (corePlayerData.getCharacter(slot) == null) return null; // character not found
        CharacterClass classType = corePlayerData.getCharacter(slot).getClassType();
        int level = corePlayerData.getCharacter(slot).getLevel();
        int exp = corePlayerData.getCharacter(slot).getExp();
        return new ClassData(uuid, classType, level, exp);
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
        }
        return false;
    }
}
