package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.common.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void resourcePackEvent(PlayerResourcePackStatusEvent e) {

        PlayerResourcePackStatusEvent.Status status = e.getStatus();
        Player player = e.getPlayer();

        if (status == PlayerResourcePackStatusEvent.Status.DECLINED) {

            // warn player
            player.sendMessage(ColorUtil.format(
                    "&4&lWARNING &c- Server resource pack disabled! We recommend using the resource pack for " +
                            "the best experience possible!"));

            // kick the player after 5 secs to give them a chance to read message.
//            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
//                @Override
//                public void run() {
//                    player.kickPlayer(ChatColor.RED + "For the best server experience, please enable the resource pack." +
//                            ChatColor.YELLOW + "\n\nMultiplayer --> For The Realm --> Edit --> Server Resource Packs: Enabled");
//                }
//            },100L);
        }

//        if (status == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
//            player.sendMessage(ChatColor.GREEN + "Resource pack loaded!");
//        }
    }
}
