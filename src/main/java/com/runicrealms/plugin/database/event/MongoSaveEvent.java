package com.runicrealms.plugin.database.event;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.ShutdownSaveWrapper;
import com.runicrealms.runicrestart.event.PreShutdownEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;
import java.util.UUID;

/**
 * This custom ASYNC event is called when the server attempts to write to Mongo.
 * Listen for this event to save all player-related session data at the same time.
 */
public class MongoSaveEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final PreShutdownEvent preShutdownEvent;
    /*
    A collection of all players to save and the slot (or slots) of the character(s) to save.
    This includes players who are not currently online!
     */
    private final Map<UUID, ShutdownSaveWrapper> playersToSave;
    private boolean isCancelled;

    /**
     * @param preShutdownEvent the associated pre shutdown event that triggered a mongo save
     */
    public MongoSaveEvent(PreShutdownEvent preShutdownEvent) {
        super(true);
        this.preShutdownEvent = preShutdownEvent;
        this.playersToSave = RunicCore.getDataAPI().getPlayersToSave();
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
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

    /**
     * @param key of the plugin matching its id in the RunicRestart config (i.e. "core")
     */
    public void markPluginSaved(String key) {
        this.preShutdownEvent.markPluginSaved(key);
    }
}
