package com.runicrealms.plugin.resourcepack;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ResourcePackManager {

    public static void openPackForPlayer(Player player) {
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

}
