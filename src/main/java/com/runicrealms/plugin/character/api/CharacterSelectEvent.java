package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.model.CharacterData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import redis.clients.jedis.Jedis;

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
    // The resource from the JedisPool. MUST BE CLOSED using 'close' after event is finished! (Done in CharacterHasLoaded)
    private final Jedis jedis;

    /**
     * @param player        who selected a character
     * @param characterData the data of that character (build from redis/mongo async)
     * @param jedis         the jedis resource
     */
    public CharacterSelectEvent(Player player, CharacterData characterData, Jedis jedis) {
        super(true);
        this.player = player;
        this.characterData = characterData;
        this.jedis = jedis;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void close() {
        this.jedis.close();
    }

    public CharacterData getCharacterData() {
        return characterData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Jedis getJedis() {
        return jedis;
    }

    public Player getPlayer() {
        return this.player;
    }

}
