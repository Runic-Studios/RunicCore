package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.rdb.RunicDatabase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.NotNull;

public class ServerListPingListener implements Listener {
    private static final String PREFIX = ColorUtil.format("                &d&lRunic Realms &r" + RunicCore.VERSION_NUMBER + "&r\n              ");
    private static String SERVER_MOTD;

    /**
     * Get the lazy init variable of the server motd
     *
     * @return lazy init variable of the server motd
     */
    @NotNull
    private static String getMOTD() {
        if (SERVER_MOTD != null) {
            return SERVER_MOTD;
        }

        String database;
        try {
            database = RunicDatabase.getDatabaseName();
        } catch (Exception e) {
            database = null;
        }

        if (database == null) {
            return PREFIX + RunicCore.VERSION_TITLE;
        }

        String description = database.equals("writer") ? ColorUtil.format("    &a&lCONTENT SERVER") : database.equals("dev") ? ColorUtil.format("  &a&lDEVELOPER SERVER") : RunicCore.VERSION_TITLE;

        SERVER_MOTD = PREFIX + description;
        return SERVER_MOTD;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        event.setMotd(getMOTD());
    }
}
