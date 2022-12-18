package com.runicrealms.plugin.api.event;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Scoreboard;
import redis.clients.jedis.Jedis;

/**
 * A custom ASYNC event which is called when a player's scoreboard will update
 *
 * @author Skyfallin
 */
public class ScoreboardUpdateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Scoreboard scoreboard;
    private final Jedis jedis;
    private String profession;
    private int professionLevel;

    /**
     * @param player     whose scoreboard will update
     * @param scoreboard of the player
     */
    public ScoreboardUpdateEvent(final Player player, final Scoreboard scoreboard) {
        super(true);
        this.player = player;
        this.scoreboard = scoreboard;
        this.jedis = RunicCore.getRedisAPI().getNewJedisResource();
        this.profession = "";
        this.professionLevel = 0;
    }

    /**
     * @param profession      string to set
     * @param professionLevel level of profession
     */
    public ScoreboardUpdateEvent(final Player player, final Scoreboard scoreboard, String profession, int professionLevel) {
        super(true);
        this.player = player;
        this.scoreboard = scoreboard;
        this.jedis = RunicCore.getRedisAPI().getNewJedisResource();
        this.profession = profession;
        this.professionLevel = professionLevel;
    }

    /**
     * @param jedis a preexisting jedis resource
     */
    public ScoreboardUpdateEvent(final Player player, final Scoreboard scoreboard, Jedis jedis) {
        super(true);
        this.player = player;
        this.scoreboard = scoreboard;
        this.jedis = jedis;
        this.profession = "";
        this.professionLevel = 0;
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

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public int getProfessionLevel() {
        return professionLevel;
    }

    public void setProfessionLevel(int professionLevel) {
        this.professionLevel = professionLevel;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }
}
