package com.runicrealms.plugin.listeners;

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

public class ResourcePackListener implements Listener {
    private static final Map<UUID, PlayerResourcePackStatusEvent.Status> STATUS = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerResourcePackStatus(PlayerResourcePackStatusEvent event) {
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        Player player = event.getPlayer();

        if (status == PlayerResourcePackStatusEvent.Status.DECLINED) {
            // warn player
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

    @NotNull
    public static PlayerResourcePackStatusEvent.Status getStatus(@NotNull Player player) {
        return STATUS.get(player.getUniqueId()); //this should never be null
    }
}
