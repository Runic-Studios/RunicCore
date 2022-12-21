package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.model.CharacterData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom ASYNC event is called when a player first selects a character from the character select screen,
 * before the data is fully loaded to and from redis
 *
 * @author Skyfallin
 */
public class CharacterSelectEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final CharacterData characterData;

    /**
     * @param player        who selected a character
     * @param characterData the data of that character (build from redis/mongo async)
     */
    public CharacterSelectEvent(Player player, CharacterData characterData) {
        super(true);
        this.player = player;
        this.characterData = characterData;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public CharacterData getCharacterData() {
        return characterData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

}
