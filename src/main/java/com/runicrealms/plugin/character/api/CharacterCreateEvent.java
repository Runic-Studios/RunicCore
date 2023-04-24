package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.model.CorePlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom ASYNC event is called when a player creates a character from the login screen
 *
 * @author Skyfallin
 */
public class CharacterCreateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    // private final CorePlayerData characterData;

    /**
     * @param player         who selected a character
     * @param corePlayerData the data of that character (build from redis/mongo async)
     */
    public CharacterCreateEvent(Player player, CorePlayerData corePlayerData) {
        super(true);
        this.player = player;
        // todo: end the event with something sync?
        // this.corePlayerData = corePlayerData;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

//    public CorePlayerData getCharacterData() {
//        return characterData;
//    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

}
