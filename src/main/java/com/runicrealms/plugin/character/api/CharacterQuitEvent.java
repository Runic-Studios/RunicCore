package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.api.RunicCoreAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import redis.clients.jedis.Jedis;

/**
 * A custom ASYNC event which is called when a player disconnects after selecting a character
 *
 * @author Skyfallin
 */
public class CharacterQuitEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final int slot;
    private final Jedis jedis;

    /**
     * @param player who quit
     * @param slot   of the character
     */
    public CharacterQuitEvent(final Player player, final int slot, boolean isAsync) {
        super(isAsync);
        this.player = player;
        this.slot = slot;
        this.jedis = RunicCoreAPI.getNewJedisResource();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public void close() {
        this.jedis.close();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Jedis getJedis() {
        return this.jedis;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getSlot() {
        return this.slot;
    }

}
