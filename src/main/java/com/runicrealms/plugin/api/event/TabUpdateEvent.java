package com.runicrealms.plugin.api.event;

import com.keenant.tabbed.tablist.TableTabList;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A custom event which is called when a player's tab will update (not called during party column health updates)
 *
 * @author Skyfallin
 */
public class TabUpdateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final TableTabList tableTabList;

    /**
     * @param player       whose scoreboard will update
     * @param tableTabList the player's table list
     */
    public TabUpdateEvent(final Player player, final TableTabList tableTabList) {
//        super(true);
        this.player = player;
        this.tableTabList = tableTabList;
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

    public TableTabList getTableTabList() {
        return tableTabList;
    }

}
