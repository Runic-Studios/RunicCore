package com.runicrealms.plugin.api;

import com.runicrealms.plugin.model.CorePlayerData;

import java.util.Map;
import java.util.UUID;

public interface PlayerDataAPI {

    /**
     * Creates a CorePlayerData object.
     * First checks Redis/Mongo for the player document.
     * Builds a new database document for the given player if it doesn't already exist when they join server/lobby
     *
     * @param uuid of player who joined
     * @return a CorePlayerData object
     */
    CorePlayerData loadCorePlayerData(UUID uuid);

    /**
     * Tries to retrieve core player data from memory
     *
     * @param uuid of player who joined
     * @return a CorePlayerData object
     */
    CorePlayerData getCorePlayerData(UUID uuid);

    /**
     * @return the map of in-memory core player data
     */
    Map<UUID, CorePlayerData> getCorePlayerDataMap();

}
