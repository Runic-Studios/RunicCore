package com.runicrealms.plugin.database.event;

import com.runicrealms.runicrestart.event.PreShutdownEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * This custom ASYNC event is called when the server attempts to write to Mongo.
 * Listen for this event to save all player-related session data at the same time.
 */
public class MongoSaveEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final PreShutdownEvent preShutdownEvent;
    private boolean isCancelled;

    /**
     * @param preShutdownEvent the associated pre shutdown event that triggered a mongo save
     */
    public MongoSaveEvent(PreShutdownEvent preShutdownEvent) {
        super(true);
        this.preShutdownEvent = preShutdownEvent;
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

    /**
     * @return a list of plugins waiting to shut down
     */
    public List<String> getPluginsToSave() {
        return this.preShutdownEvent.getPluginsToSave();
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
