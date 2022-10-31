package com.runicrealms.plugin.database.event;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.ShutdownSaveWrapper;
import com.runicrealms.runicrestart.event.PreShutdownEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.UUID;

/**
 * This custom event is called when the server attempts to write to Mongo.
 * Listen for this event to save all player-related session data at the same time.
 */
public class MongoSaveEvent extends Event implements Cancellable {

    private final PreShutdownEvent preShutdownEvent;
    private final Jedis jedis; // the jedis resource to read from
    /*
    A collection of all players to save and the slot (or slots) of the character(s) to save. This includes players who are not currently online!
     */
    private final Map<UUID, ShutdownSaveWrapper> playersToSave;
    private boolean isCancelled;

    /**
     * @param preShutdownEvent
     * @param jedis
     */
    public MongoSaveEvent(PreShutdownEvent preShutdownEvent, Jedis jedis) {
        this.preShutdownEvent = preShutdownEvent;
        this.jedis = jedis;
        this.playersToSave = RunicCore.getDatabaseManager().getPlayersToSave();
        this.isCancelled = false;
    }

    /**
     * @param key
     */
    public void markPluginSaved(String key) {
        this.preShutdownEvent.markPluginSaved(key);
    }

    public Jedis getJedis() {
        return this.jedis;
    }

    public Map<UUID, ShutdownSaveWrapper> getPlayersToSave() {
        return playersToSave;
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
