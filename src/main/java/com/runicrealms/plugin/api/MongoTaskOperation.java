package com.runicrealms.plugin.api;

import com.mongodb.bulk.BulkWriteResult;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.CharacterField;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.UUID;

/**
 * A MongoTask is used in each plugin handling persistent data to periodically "flush" the Redis
 * cache and propagate the data to Mongo
 *
 * @author Skyfallin, Destiny
 */
public interface MongoTaskOperation {

    /**
     * @return the name of the plugin's collection in MongoDB
     */
    String getCollectionName();

    /**
     * @return a set of uuids of player marked for save
     */
    default String getJedisSet() {
        String database = RunicCore.getDataAPI().getMongoDatabase().getName();
        return database + ":markedForSave:" + getCollectionName();
    }

    /**
     * Most plugins will use this default query to find the correct document.
     * Documents are indexed on PLAYER_UUID
     *
     * @param uuid of the player
     * @return a query for this uuid
     */
    default Query getQuery(UUID uuid) {
        return new Query(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
    }

    /**
     * Gets the intended mutation for a mongo document
     *
     * @param object some mapped spring document (e.g. CorePlayerData)
     * @param <T>    an update of the specified object type
     * @return an Update object to send to Mongo
     */
    <T> Update getUpdate(T object);

    /**
     * Saves all players 'markedForSave' to MongoDB
     *
     * @param callback a function to execute after the write operation
     */
    void saveAllToMongo(WriteCallback callback);

    /**
     * Sends a single bulk operation of mutations for documents in MongoDB
     *
     * @return acknowledged if documents were successfully modified
     */
    BulkWriteResult sendBulkOperation();

}
