package com.runicrealms.plugin.donor.boost.event;

import com.runicrealms.plugin.donor.boost.api.Boost;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BoostEndEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final String activatorName;
    private final UUID activatorUUID;
    private final Boost boost;

    public BoostEndEvent(String activatorName, UUID activatorUUID, Boost boost) {
        this.activatorName = activatorName;
        this.activatorUUID = activatorUUID;
        this.boost = boost;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public String getActivatorName() {
        return this.activatorName;
    }

    public UUID getActivatorUUID() {
        return this.activatorUUID;
    }

    public Boost getBoost() {
        return this.boost;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}