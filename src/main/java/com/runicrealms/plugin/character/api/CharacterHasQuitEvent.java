package com.runicrealms.plugin.character.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when the data from a CorePlayerData object is fully
 * synchronized to the global cache or database. Used to prevent players from logging
 * in while their data is saving
 *
 * @author Skyfallin
 */
public class CharacterHasQuitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final CharacterQuitEvent characterQuitEvent;

    public CharacterHasQuitEvent(Player player, CharacterQuitEvent characterQuitEvent) {
        this.player = player;
        this.characterQuitEvent = characterQuitEvent;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public CharacterQuitEvent getCharacterQuitEvent() {
        return characterQuitEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

}
