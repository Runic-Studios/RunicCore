package com.runicrealms.plugin.character.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when the data from a CorePlayerData object is fully
 * written to or from redis, and is used to remove helper objects from memory
 *
 * @author Skyfallin
 */
public class CharacterLoadedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final CharacterSelectEvent characterSelectEvent;

    public CharacterLoadedEvent(Player player, CharacterSelectEvent characterSelectEvent) {
        this.player = player;
        this.characterSelectEvent = characterSelectEvent;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public CharacterSelectEvent getCharacterSelectEvent() {
        return characterSelectEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

}
