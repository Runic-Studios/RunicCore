package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListPingListener implements Listener {

    private static String SERVER_MOTD;

    static {
        int port = Bukkit.getPort();
        switch (port) {
            case 25566:
                SERVER_MOTD = ColorUtil.format("                    &d&lRUNIC REALMS&r" + "\n              &a&lWRITER SERVER");
            case 25567:
                SERVER_MOTD = ColorUtil.format("                    &d&lRUNIC REALMS&r" + "\n              &a&lBUILD SERVER");
            case 25568:
                SERVER_MOTD = ColorUtil.format("                    &d&lRUNIC REALMS&r" + "\n              &a&lDEV SERVER");
            default:
                SERVER_MOTD = ColorUtil.format("                    &d&lRUNIC REALMS&r" + "\n              &a&l2.0 - The Second Age!");
        }
    }

    @EventHandler
    public void onServerListPing(final ServerListPingEvent event) {
        event.setMotd(SERVER_MOTD);
    }
}
