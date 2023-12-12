package com.runicrealms.plugin.party.event;

import com.runicrealms.plugin.party.Party;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a party is formed or disbanded.
 */
public abstract class PartyEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Party party;
    private boolean isCancelled;

    public PartyEvent(Party party) {
        this.party = party;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Party getParty() {
        return party;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
