package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

/**
 * This custom ASYNC event is called when a player deletes a character from the login screen
 *
 * @author Skyfallin
 */
public class CharacterDeleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final int slot;
    /*
    Uses our standard "handshake" system to prevent event completion until all plugins
    have removed their data
     */
    private final List<String> pluginsToDeleteData;

    /**
     * @param player who selected a character
     * @param slot   of the character
     */
    public CharacterDeleteEvent(Player player, int slot) {
        super(true);
        this.player = player;
        this.slot = slot;
        // Removes player from the save task for core
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            String database = RunicCore.getDataAPI().getMongoDatabase().getName();
            jedis.srem(database + ":markedForSave:core", String.valueOf(player.getUniqueId()));
        }
        pluginsToDeleteData = new ArrayList<>() {{
            add("core");
        }};
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    public List<String> getPluginsToDeleteData() {
        return pluginsToDeleteData;
    }

    public int getSlot() {
        return slot;
    }
}
