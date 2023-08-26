package com.runicrealms.plugin.resourcepack;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private static final Map<UUID, PlayerResourcePackStatusEvent.Status> STATUS = new HashMap<>();

    public static void openPackForPlayer(@NotNull Player player) {
        ViaAPI api = Via.getAPI(); // Get the API
        int version = api.getPlayerVersion(player); // Get the protocol version
        ResourcePackVersion pack = ResourcePackVersion.getFromVersionNumber(version);
        if (pack != null) {
            //Bukkit.broadcastMessage(pack.name());
            player.setResourcePack(pack.getLink());
        } else {
            Bukkit.getLogger().info(ChatColor.RED + "ERROR: We couldn't find a resource pack for a player's minecraft version!");
        }
    }

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

    public static boolean isPackActive(@NotNull Player player) {
        return getStatus(player) == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED;
    }
}
