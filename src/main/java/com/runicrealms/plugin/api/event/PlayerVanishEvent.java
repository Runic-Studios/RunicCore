package com.runicrealms.plugin.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event used when a player enters or exits vanish
 *
 * @author BoBoBalloon
 * @since 6/26/23
 */
public class PlayerVanishEvent extends Event {

    private final Player player;

    public PlayerVanishEvent(@NotNull Player player) {
        this.player = player;
    }

    @NotNull
    public Player getPlayer() {
        return this.player;
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
