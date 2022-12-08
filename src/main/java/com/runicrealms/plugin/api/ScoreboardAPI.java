package com.runicrealms.plugin.api;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public interface ScoreboardAPI {

    /**
     * Set the scoreboard for the given player if they do not yet have one
     *
     * @param player to receive scoreboard
     */
    void setupScoreboard(final Player player);

    /**
     * Method used to keep scoreboard accurate during level-up, profession change, etc.
     * Some string manipulation because scoreboard teams can't go beyond 16 chars
     *
     * @param player     who owns the scoreboard
     * @param scoreboard the scoreboard of the player
     */
    void updatePlayerInfo(final Player player, final Scoreboard scoreboard);

    /**
     * Used so that other plugins can trigger a scoreboard update
     *
     * @param player the player to update
     */
    void updatePlayerScoreboard(Player player);
}
