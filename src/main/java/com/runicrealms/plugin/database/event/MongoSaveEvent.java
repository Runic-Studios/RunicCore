package com.runicrealms.plugin.database.event;

import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * This custom event is called when the server attempts to write to Mongo.
 * Listen for this event to save all player-related session data at the same time.
 */
public class MongoSaveEvent extends Event implements Cancellable {

    private final int slot;
    private final UUID uuid;
    private final Jedis jedis; // the jedis resource to read from
    private final PlayerMongoData mongoData;
    private final PlayerMongoDataSection mongoDataSection;
    private final CacheSaveReason cacheSaveReason;
    private boolean isCancelled;

    /**
     * @param slot             the slot of the character
     * @param uuid             of the player
     * @param jedis            the jedis resource
     * @param mongoData        object file of player in DB
     * @param mongoDataSection section of object file being saved (typically character section)
     * @param cacheSaveReason  why the cache was saved (logout, shutdown, etc.)
     */
    public MongoSaveEvent(int slot,
                          UUID uuid,
                          Jedis jedis,
                          PlayerMongoData mongoData,
                          PlayerMongoDataSection mongoDataSection,
                          CacheSaveReason cacheSaveReason) {
        this.slot = slot;
        this.uuid = uuid;
        this.jedis = jedis;
        this.mongoData = mongoData;
        this.mongoDataSection = mongoDataSection;
        this.cacheSaveReason = cacheSaveReason;
        this.isCancelled = false;
    }

    public int getSlot() {
        return this.slot;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public Jedis getJedis() {
        return this.jedis;
    }

    public PlayerMongoData getMongoData() {
        return this.mongoData;
    }

    public PlayerMongoDataSection getMongoDataSection() {
        return this.mongoDataSection;
    }

    public CacheSaveReason cacheSaveReason() {
        return this.cacheSaveReason;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    private static final HandlerList handlers = new HandlerList();

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
