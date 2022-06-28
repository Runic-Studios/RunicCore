package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This custom event is called when a player first selects a character from the character select screen,
 * before the data is fully loaded to and from redis
 *
 * @author Skyfallin
 */
public class CharacterSelectEvent extends Event {

    private final PlayerCache cache; // todo: remove
    private final Player player;
    private final CharacterData characterData;

    private static final HandlerList handlers = new HandlerList();

    public CharacterSelectEvent(PlayerCache cache, Player player, CharacterData characterData) {
        this.cache = cache;
        this.player = player;
        this.characterData = characterData;
    }

    public PlayerCache getPlayerCache() {
        return this.cache;
    }

    public Player getPlayer() {
        return this.player;
    }

    public CharacterData getCharacterData() {
        return characterData;
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
