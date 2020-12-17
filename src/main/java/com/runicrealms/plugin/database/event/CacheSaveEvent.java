package com.runicrealms.plugin.database.event;

import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * This custom event is called when the cache manager attempts to write to Mongo.
 * Listen for this event to save all player-related persistent data at the same time.
 */
public class CacheSaveEvent extends Event implements Cancellable {

    private final Player player;
    private final PlayerMongoData mongoData;
    private final PlayerMongoDataSection mongoDataSection;
    private final CacheSaveReason cacheSaveReason;
    private boolean isCancelled;

    /**
     *
     * @param player player of cache
     * @param mongoData object file of player in DB
     * @param mongoDataSection section of object file being saved (typically character section)
     * @param cacheSaveReason why the cache was saved (logout, shutdown, etc.)
     */
    public CacheSaveEvent(Player player, PlayerMongoData mongoData, PlayerMongoDataSection mongoDataSection,
                          CacheSaveReason cacheSaveReason) {
        this.player = player;
        this.mongoData = mongoData;
        this.mongoDataSection = mongoDataSection;
        this.cacheSaveReason = cacheSaveReason;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return this.player;
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
