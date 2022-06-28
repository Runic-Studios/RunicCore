package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.model.CharacterData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when the data from a CharacterData object is fully
 * written to or from redis, and is used to remove helper objects from memory
 *
 * @author Skyfallin
 */
public class CharacterLoadedEvent extends Event {

    private final Player player;
    private final CharacterData characterData;

    private static final HandlerList handlers = new HandlerList();

    public CharacterLoadedEvent(Player player, CharacterData characterData) {
        this.player = player;
        this.characterData = characterData;
    }

    public Player getPlayer() {
        return this.player;
    }

    public CharacterData getCharacterData() {
        return characterData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
