package com.runicrealms.plugin.api.event;

import com.runicrealms.plugin.spellapi.spelltypes.ShieldPayload;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This custom ASYNC event is called when a player's spell shield is broken
 */
public class ShieldBreakEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final ShieldPayload shieldPayload;
    private final BreakReason breakReason;
    private boolean isCancelled;
    /**
     * @param shieldPayload containing player, source, shield
     */
    public ShieldBreakEvent(@NotNull ShieldPayload shieldPayload, BreakReason breakReason) {
        this.shieldPayload = shieldPayload;
        this.breakReason = breakReason;
        this.isCancelled = false;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ShieldPayload getShieldPayload() {
        return shieldPayload;
    }

    public BreakReason getBreakReason() {
        return breakReason;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }

    public enum BreakReason {
        DAMAGE,
        FALLOFF
    }

}
