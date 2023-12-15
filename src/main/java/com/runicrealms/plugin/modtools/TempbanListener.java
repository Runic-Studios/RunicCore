package com.runicrealms.plugin.modtools;

import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TempbanListener implements Listener {

    private final Map<UUID, Long> tempUnbanTimestamps = new HashMap<>();

    private final Map<UUID, Long> unTempBannedMessage = new HashMap<>();

    public static String prettyPrintMillis(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(days != 1 ? " days " : " day ");
        if (hours > 0) sb.append(hours).append(hours != 1 ? " hours " : " hour ");
        sb.append(minutes).append(minutes != 1 ? " minutes" : " minute");
        return sb.toString().trim();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPermission("runic.tempbanned") && !event.getPlayer().isOp()) {
            final UUID target = event.getPlayer().getUniqueId();
            if (!tempUnbanTimestamps.containsKey(event.getPlayer().getUniqueId())) {
                RunicCommon.getLuckPermsAPI().retrieveData(event.getPlayer().getUniqueId()).then(data -> {
                    if (data.containsKey("runic.tempban.timestamp"))
                        tempUnbanTimestamps.put(target, data.getLong("runic.tempban.timestamp"));
                });
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.RED + "You have been temporarily banned! To see the duration" +
                        "\nof your ban, rejoin the server. To appeal this ban," +
                        "\ncontact a moderator on our discord server.");
            } else {
                long duration = tempUnbanTimestamps.get(event.getPlayer().getUniqueId()) - System.currentTimeMillis();
                RunicCommon.getLuckPermsAPI().retrieveData(event.getPlayer().getUniqueId()).then(data -> {
                    if (data.containsKey("runic.tempban.timestamp"))
                        tempUnbanTimestamps.put(target, data.getLong("runic.tempban.timestamp"));
                });
                if (duration < 0) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + event.getPlayer().getName() + " permission unset runic.tempbanned");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + event.getPlayer().getName() + " meta unset runic.tempban.timestamp");
                    duration *= -1;
                    if (duration > 60000) {
                        event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Your tempban expired " + prettyPrintMillis(duration) + " ago.");
                    } else {
                        event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Your tempban recently expired.");
                    }
                    unTempBannedMessage.put(event.getPlayer().getUniqueId(), duration);
                    tempUnbanTimestamps.remove(event.getPlayer().getUniqueId());
                } else {
                    event.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.RED + "You have been temporarily banned for " + prettyPrintMillis(duration) +
                            ".\nTo appeal this ban, contact a moderator on our discord server.");
                }
            }
        }
    }

    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        if (unTempBannedMessage.containsKey(event.getPlayer().getUniqueId())) {
            long duration = unTempBannedMessage.get(event.getPlayer().getUniqueId());
            if (duration > 60000) {
                event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Your tempban expired " + prettyPrintMillis(duration) + " ago.");
            } else {
                event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Your tempban recently expired.");
            }
            unTempBannedMessage.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        unTempBannedMessage.remove(event.getPlayer().getUniqueId());
    }

}
