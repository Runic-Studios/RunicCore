package com.runicrealms.plugin.character.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when the data from a CharacterData object is fully
 * synchronized to the global cache or database. Used to prevent players from logging
 * in while their data is saving
 *
 * @author Skyfallin
 */
public class CharacterHasQuitEvent extends Event {

    private final Player player;
    private final CharacterQuitEvent characterQuitEvent;

    private static final HandlerList handlers = new HandlerList();

    public CharacterHasQuitEvent(Player player, CharacterQuitEvent characterQuitEvent) {
        this.player = player;
        this.characterQuitEvent = characterQuitEvent;
    }

    public Player getPlayer() {
        return this.player;
    }

    public CharacterQuitEvent getCharacterQuitEvent() {
        return characterQuitEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
