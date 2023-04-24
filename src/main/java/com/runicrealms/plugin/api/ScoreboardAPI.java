package com.runicrealms.plugin.api;

import com.runicrealms.plugin.api.event.ScoreboardUpdateEvent;
import org.bukkit.entity.Player;

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
     * @param player who owns the scoreboard
     * @param event  the scoreboard update event that was triggered async
     */
    void updatePlayerInfo(final Player player, final ScoreboardUpdateEvent event);

    /**
     * Used so that other plugins can trigger a scoreboard update event
     *
     * @param player the player to update
     */
    void updatePlayerScoreboard(Player player);
}
