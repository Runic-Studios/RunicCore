package com.runicrealms.plugin.api;

import com.keenant.tabbed.tablist.TableTabList;
import com.runicrealms.plugin.common.util.Pair;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public interface TabAPI {

    /**
     * Gets the players ping by using NMS to access the internal 'ping' field in
     * EntityPlayer
     *
     * @param player the player whose ping to get
     * @return the player's ping
     */
    int getPing(Player player);

    /**
     * Creates a getter for the player tablist so other plugins can edit it
     *
     * @param player to get tablist for
     * @return a tablist object
     */
    TableTabList getPlayerTabList(Player player);

    /**
     * Initializes the 'global' and 'friends' sections of the tablist
     *
     * @param player to set up tab for
     */
    void setupTab(Player player);

    /**
     * Refreshs everyone's tablists
     */
    void refreshAllTabLists();

    /**
     * Gets a player's name color in for the tab list.
     */
    String getTablistNameColor(Player player);

    /**
     * Sorts the players in the tab-list by their name color (i.e. rank).
     * Returns a list pairs of players and their colored names in order.
     */
    List<Pair<? extends Player, String>> sortPlayersByRank(Collection<? extends Player> players);
}
