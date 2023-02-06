package com.runicrealms.plugin.api.event;

import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called whenever a custom RunicStatusEffect is applied (stun, root, etc.)
 */
public class StatusEffectEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private RunicStatusEffect runicStatusEffect;
    private boolean isCancelled;

    /**
     * @param player
     * @param runicStatusEffect
     */
    public StatusEffectEvent(Player player, RunicStatusEffect runicStatusEffect) {
        this.player = player;
        this.runicStatusEffect = runicStatusEffect;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }
}
