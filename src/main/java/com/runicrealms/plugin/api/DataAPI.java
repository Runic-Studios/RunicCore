package com.runicrealms.plugin.api;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.runicrealms.plugin.database.ShutdownSaveWrapper;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.model.PlayerData;
import org.bson.Document;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface DataAPI {

    /**
     * Adds a new mongo document (new players) and puts it into the lookup map in memory
     *
     * @param uuid uuid to string of the player to add
     * @return the newly-added document
     */
    Document addNewDocument(String uuid);

    /**
     * Checks redis to see if the currently selected character's data is cached.
     * And if it is, returns the CharacterData object
     *
     * @param uuid  of player to check
     * @param slot  of the character
     * @param jedis the jedis resource (from character select or quit event)
     * @return a CharacterData object if it is found in redis
     */
    CharacterData checkRedisForCharacterData(UUID uuid, Integer slot, Jedis jedis);

    /**
     * @return a collection of all guild documents
     */
    MongoCollection<Document> getGuildDocuments();

    /**
     * @return the database specified in config (live, dev, etc.)
     */
    MongoDatabase getMongoDatabase();

    /**
     * Gets a copy of the PlayerData object from the database manager
     * NOTE: this object is destroyed once the player loads their character!
     * Only use it for login and select-based logic
     *
     * @param uuid of the player
     * @return their data wrapper object (no character data)
     */
    PlayerData getPlayerData(UUID uuid);

    /**
     * @return a map of uuid to PlayerData object wrapper
     */
    Map<UUID, PlayerData> getPlayerDataMap();

    /**
     * @return a map of uuid (as string) to a mongo document for player entity
     */
    HashMap<String, Document> getPlayerDocumentMap();

    /**
     * @return a map of uuid to their shutdown wrapper, which maintains a collection of players to save
     */
    Map<UUID, ShutdownSaveWrapper> getPlayersToSave();

    /**
     * Checks the entire player_data collection for the given document.
     * (For use if player has not been loaded into data structure for last 30 days).
     * I believe there is a way to optimize this by just returning the cursor using limit()
     *
     * @param uuid of the player to lookup
     * @return true if the player is in the collection
     */
    boolean isInCollection(String uuid);

    /**
     * Builds a new database document for the given player if it doesn't already exist when they join server/lobby
     *
     * @param player who joined
     * @return a PlayerData object
     */
    PlayerData loadPlayerData(Player player, Jedis jedis);

    /**
     * WARNING: should only be called AFTER checking if the document is in the collection using 'isInCollection'
     *
     * @param uuid of the player to lookup
     * @return the document (if found, older than 30 days) or null
     */
    Document retrieveDocumentFromCollection(String uuid);
}
