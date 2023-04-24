package com.runicrealms.plugin.api;

import com.mongodb.client.MongoDatabase;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.model.ProjectedData;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Map;
import java.util.UUID;

public interface DataAPI {

    /**
     * Adds the CorePlayerData object to memory
     *
     * @param corePlayerData from redis/mongo
     */
    void addToCoreDataMap(CorePlayerData corePlayerData);

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

    /**
     * @return The max char slot, which is how many characters we can have in the game
     * * e.g. 10
     */
    int getMaxCharacterSlot();

    /**
     * @return the database specified in config (live, dev, etc.)
     */
    MongoDatabase getMongoDatabase();

    /**
     * Returns the single instance of the mongo template.
     * It's thread-safe
     *
     * @return a MongoTemplate using our MongoClient and MongoDatabase
     */
    MongoTemplate getMongoTemplate();

    /**
     * Gets a copy of the ProjectedData map from the database manager
     * NOTE: these objects are destroyed once the player loads their character!
     * Only use this reference for login and select-based logic
     *
     * @return their data wrapper object (no character data)
     */
    Map<UUID, ProjectedData> getProjectedDataMap();

    /**
     * Creates a CorePlayerData object.
     * First checks Redis/Mongo for the player document.
     * Builds a new database document for the given player if it doesn't already exist when they join server/lobby
     *
     * @param uuid of player who joined
     * @return a CorePlayerData object
     */
    CorePlayerData loadCorePlayerData(UUID uuid);

}
