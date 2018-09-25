package us.fortherealm.plugin.listeners;

import us.fortherealm.plugin.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackEvent implements Listener {

    private Main plugin = Main.getInstance();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPackDecline (PlayerResourcePackStatusEvent e) {

        PlayerResourcePackStatusEvent.Status status = e.getStatus();
        Player player = e.getPlayer();
        if (status == PlayerResourcePackStatusEvent.Status.DECLINED) {

            player.sendMessage(ChatColor.RED + "WARNING: Server resource pack disabled. You will be kicked in 5 seconds!");

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    player.kickPlayer(ChatColor.RED + "For the best server experience, please enable the resource pack." +
                            ChatColor.YELLOW + "\n\nMultiplayer --> For the Realm --> Edit --> Server Resource Packs: Enabled");
                }
            },100L);
        }
    }
}
