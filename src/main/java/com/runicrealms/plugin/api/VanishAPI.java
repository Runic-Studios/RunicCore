package com.runicrealms.plugin.api;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface VanishAPI {

    /**
     * Gets all players currently vanished on the network
     */
    Collection<Player> getVanishedPlayers();

    /**
     * Makes a player vanish from other player's screens and the tablist
     */
    void hidePlayer(Player player);

    /**
     * Makes a player reappear
     */
    void showPlayer(Player player);

}
