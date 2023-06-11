package com.runicrealms.plugin.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scoreboard.Scoreboard;

/**
 * A custom ASYNC event which is called when a player's scoreboard will update
 *
 * @author Skyfallin
 */
public class ScoreboardUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Scoreboard scoreboard;
    private String profession;
    private int professionLevel;
    private String guild;
    private boolean isOutlaw;

    /**
     * @param player     whose scoreboard will update
     * @param scoreboard of the player
     */
    public ScoreboardUpdateEvent(final Player player, final Scoreboard scoreboard) {
        super(true);
        this.player = player;
        this.scoreboard = scoreboard;
        this.profession = "";
        this.professionLevel = 0;
        this.guild = "";
        this.isOutlaw = false;
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

    public boolean isOutlaw() {
        return isOutlaw;
    }

    public void setOutlaw(boolean outlaw) {
        isOutlaw = outlaw;
    }

    public String getGuild() {
        return guild;
    }

    public void setGuild(String guild) {
        this.guild = guild;
    }
}
