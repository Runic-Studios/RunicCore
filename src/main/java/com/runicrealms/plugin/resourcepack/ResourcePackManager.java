package com.runicrealms.plugin.resourcepack;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

public class ResourcePackManager {

    public static void openPackForPlayer(Player player) {
        ViaAPI api = Via.getAPI(); // Get the API
        int version = api.getPlayerVersion(player); // Get the protocol version
        ResourcePackVersion pack = ResourcePackVersion.getFromVersionNumber(version);
        if (pack != null) {
            player.setResourcePack(pack.getLink());
        } else {
            player.sendMessage(ChatColor.RED + "We couldn't find a resource pack for your minecraft version!");
        }
    }

}
