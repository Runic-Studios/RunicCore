package com.runicrealms.plugin.api;

import com.keenant.tabbed.tablist.TableTabList;
import org.bukkit.entity.Player;

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
}
