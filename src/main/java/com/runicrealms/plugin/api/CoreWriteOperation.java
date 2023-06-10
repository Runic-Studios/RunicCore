package com.runicrealms.plugin.api;

import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.rdb.api.WriteCallback;

import java.util.UUID;

/**
 * Used for efficient updating of mongo document fields
 */
public interface CoreWriteOperation {

    /**
     * Updates the ENTIRE CorePlayerData document in MongoDB
     *
     * @param slot     of the character (for character-specific fields)
     * @param newValue a new CorePlayerData object
     */
    void updateCorePlayerData(UUID uuid, int slot, CorePlayerData newValue, WriteCallback callback);

    /**
     * Updates a single field that is account-wide in the mapped CorePlayerData document object
     *
     * @param uuid      of the player
     * @param fieldName of the document "titleData"
     * @param newValue  the new value for the field
     * @param callback  a sync function to execute when TaskChain is complete
     */
    <T> void updateCorePlayerData(UUID uuid, String fieldName, T newValue, WriteCallback callback);

    /**
     * Updates a single field of the mapped 'CorePlayerData' document object
     *
     * @param fieldName of the document "skillTreeDataMap"
     * @param newValue  the new value for the field
     * @param <T>       the type of object to set as the field value "SkillTreeData"
     */
    <T> void updateCorePlayerData(UUID uuid, int slot, String fieldName, T newValue, WriteCallback callback);

    /**
     * Updates a single field of the mapped 'CoreCharacterData' document object
     *
     * @param uuid     of the player
     * @param slot     of the character
     * @param newValue the new value for the field
     * @param <T>      the type of object to set as the field value "SkillTreeData"
     * @param callback a function to execute on main thread when write operation is complete
     */
    <T> void updateCoreCharacterData(UUID uuid, int slot, T newValue, WriteCallback callback);

}
