package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CharacterQuitEvent extends Event {

    private PlayerCache cache;
    private Player player;

    private static final HandlerList handlers = new HandlerList();

    public CharacterQuitEvent(PlayerCache cache, Player player) {
        this.cache = cache;
        this.player = player;
    }

    public PlayerCache getPlayerCache() {
        return this.cache;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Integer getSlot() {
        return this.cache.getCharacterSlot();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
