package com.runicrealms.plugin.model;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.model.CharacterField;
import org.bukkit.entity.Player;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This intermediary object acts as an easy way to retrieve fields from Mongo
 * It is used during login to both display data in the character select screen and load basic info for the player
 * on character select, then it is removed after CharacterLoadEvent
 *
 * @author Skyfallin
 */
public class ProjectedData {
    private final UUID uuid;
    private final Map<Integer, ClassData> playerCharacters;

    public ProjectedData(Player player) {
        this.uuid = player.getUniqueId();
        this.playerCharacters = new HashMap<>();
        try {
            for (int i = 1; i <= RunicDatabase.getAPI().getDataAPI().getMaxCharacterSlot(); i++) {
                ClassData result = loadClassData(i);
                if (result != null) {
                    playerCharacters.put(i, result);
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
        for (int i = 1; i <= RunicDatabase.getAPI().getDataAPI().getMaxCharacterSlot(); i++) {
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
        CorePlayerData corePlayerData = RunicDatabase.getAPI().getDataAPI().getMongoTemplate().findOne(query, CorePlayerData.class);
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

}
