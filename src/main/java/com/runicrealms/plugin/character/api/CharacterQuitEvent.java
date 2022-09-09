package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.api.RunicCoreAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import redis.clients.jedis.Jedis;

/**
 * A custom event which is called when a player disconnects after selecting a character
 *
 * @author Skyfallin
 */
public class CharacterQuitEvent extends Event {

    private final Player player;
    private final int slot;
    private final Jedis jedis;

    private static final HandlerList handlers = new HandlerList();

    /**
     * @param player
     * @param slot
     */
    public CharacterQuitEvent(final Player player, final int slot) {
        this.player = player;
        this.slot = slot;
        this.jedis = RunicCoreAPI.getNewJedisResource();
    }

    /**
     * @param player
     * @param slot
     * @param jedis
     */
    public CharacterQuitEvent(final Player player, final int slot, final Jedis jedis) {
        this.player = player;
        this.slot = slot;
        this.jedis = jedis;
    }

    public void close() {
        this.jedis.close();
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getSlot() {
        return this.slot;
    }

    public Jedis getJedis() {
        return this.jedis;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
