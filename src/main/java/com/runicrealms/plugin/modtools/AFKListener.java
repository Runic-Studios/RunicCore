package com.runicrealms.plugin.modtools;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AFKListener implements Listener {

    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();

    private final Map<UUID, Long> lastMoved = new ConcurrentHashMap<>();

    private final int AFK_KICK_MILLIS = RunicCore.getInstance().getConfig().getInt("afk-kick-seconds") * 1000;

    public AFKListener() {
        // This has been hyper optimized, please don't touch
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            long currentTime = System.currentTimeMillis();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("runiccore.antiafk")) continue;
                Location location = player.getLocation();
                Location lastLocation = lastLocations.get(player.getUniqueId());
                if (lastLocation != null && !lastLocation.equals(location)) {
                    lastMoved.put(player.getUniqueId(), currentTime);
                } else {
                    Long lastMovedTime = lastMoved.get(player.getUniqueId());
                    if (lastMovedTime != null && lastMovedTime + AFK_KICK_MILLIS < currentTime) {
                        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> player.kickPlayer(ChatColor.RED + "You have been idling for too long!"));
                    }
                }
                lastLocations.put(player.getUniqueId(), location);
            }
        }, 20 * 5, 20 * 5);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastLocations.remove(event.getPlayer().getUniqueId());
        lastMoved.remove(event.getPlayer().getUniqueId());
    }

}
