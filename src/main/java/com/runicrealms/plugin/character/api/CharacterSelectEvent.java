package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.model.CharacterData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import redis.clients.jedis.Jedis;

/**
 * This custom event is called when a player first selects a character from the character select screen,
 * before the data is fully loaded to and from redis
 *
 * @author Skyfallin
 */
public class CharacterSelectEvent extends Event {

    private final Player player;
    private final CharacterData characterData;
    // The resource from the JedisPool. MUST BE CLOSED using 'close' after event is finished!
    private final Jedis jedis;

    private static final HandlerList handlers = new HandlerList();

    /**
     * @param player
     * @param characterData
     */
    public CharacterSelectEvent(Player player, CharacterData characterData) {
        this.player = player;
        this.characterData = characterData;
        this.jedis = RunicCoreAPI.getNewJedisResource();
    }

    /**
     * @param player
     * @param characterData
     * @param jedis
     */
    public CharacterSelectEvent(Player player, CharacterData characterData, Jedis jedis) {
        this.player = player;
        this.characterData = characterData;
        this.jedis = jedis;
    }

    public void close() {
        this.jedis.close();
    }

    public Player getPlayer() {
        return this.player;
    }

    public CharacterData getCharacterData() {
        return characterData;
    }

    public Jedis getJedis() {
        return jedis;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
