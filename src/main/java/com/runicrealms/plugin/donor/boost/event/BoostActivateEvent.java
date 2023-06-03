package com.runicrealms.plugin.donor.boost.event;

import com.runicrealms.plugin.donor.boost.api.Boost;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BoostActivateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player activator;
    private final Boost boost;

    public BoostActivateEvent(Player activator, Boost boost) {
        this.activator = activator;
        this.boost = boost;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getActivator() {
        return this.activator;
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
