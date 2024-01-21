package com.runicrealms.plugin.resourcepack;

import com.runicrealms.plugin.common.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResourcePackManager implements Listener {
    private static final String URL = "https://www.dropbox.com/scl/fi/li37h7klt2ejwqmnz9w14/RR-Resourcepack-01.21.24.zip?rlkey=cpp6bbapf9690nnmf5pa6vbr3&dl=1";
    private static final Map<UUID, PlayerResourcePackStatusEvent.Status> STATUS = new HashMap<>();

    public static void openPackForPlayer(@NotNull Player player) {
        player.setResourcePack(URL);
    }

    @NotNull
    public static PlayerResourcePackStatusEvent.Status getStatus(@NotNull Player player) {
        return STATUS.get(player.getUniqueId()); //this should never be null
    }

    public static boolean isPackActive(@NotNull Player player) {
        return getStatus(player) == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event) {
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        Player player = event.getPlayer();

        if (status == PlayerResourcePackStatusEvent.Status.DECLINED) {
            // Warn player
            player.sendMessage(ColorUtil.format(
                    "&4&lWARNING &c- Server resource pack disabled! We recommend using the resource pack for " +
                            "the best experience possible!"));
        }

        STATUS.put(event.getPlayer().getUniqueId(), status);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        STATUS.remove(event.getPlayer().getUniqueId());
    }
}
