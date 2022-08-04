package com.runicrealms.plugin.database.event;

import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * This custom event is called when the server attempts to write to Mongo.
 * Listen for this event to save all player-related session data at the same time.
 */
public class MongoSaveEvent extends Event implements Cancellable {

    private final int slot;
    private final UUID uuid;
    private final PlayerMongoData mongoData;
    private final PlayerMongoDataSection mongoDataSection;
    private final CacheSaveReason cacheSaveReason;
    private boolean isCancelled;

    /**
     * @param slot             the slot of the character
     * @param uuid             of the player
     * @param mongoData        object file of player in DB
     * @param mongoDataSection section of object file being saved (typically character section)
     * @param cacheSaveReason  why the cache was saved (logout, shutdown, etc.)
     */
    public MongoSaveEvent(int slot, UUID uuid, PlayerMongoData mongoData, PlayerMongoDataSection mongoDataSection,
                          CacheSaveReason cacheSaveReason) {
        this.slot = slot;
        this.uuid = uuid;
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
