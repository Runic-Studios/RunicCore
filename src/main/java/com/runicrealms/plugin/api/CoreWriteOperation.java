package com.runicrealms.plugin.api;

import com.runicrealms.plugin.rdb.api.WriteCallback;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.UUID;

/**
 * Used for efficient updating of mongo document fields
 */
public interface CoreWriteOperation {

    /**
     * Updates a single field of the mapped 'CorePlayerData' document object
     *
     * @param uuid          of the player
     * @param slot          of the character
     * @param fieldName     of the document "skillTreeDataMap"
     * @param newValue      the new value for the field
     * @param mongoTemplate the global mongo template
     * @param <T>           the type of object to set as the field value "SkillTreeData"
     * @param callback      a function to execute on main thread when write operation is complete
     */
    <T> void updateCorePlayerData(UUID uuid, int slot, String fieldName, T newValue, MongoTemplate mongoTemplate, WriteCallback callback);

}
