package us.fortherealm.plugin.nametags;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;

public class PlayerNameManager implements Listener {

    private Plugin plugin = FTRCore.getInstance();
    // TODO: add this to main
    // TODO: names need to reset on serve disable
    private NameTagChanger nameTagChanger = new NameTagChanger();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();

        // grab the player's name from their UUID
        String name = Bukkit.getPlayer(player.getUniqueId()).getName();

        // store the player's name for future use
        plugin.getConfig().set(player.getUniqueId() + ".info.name", name);
        plugin.saveConfig();
        plugin.reloadConfig();

        // updates all outlaw names for the joiner if other online player is outlaw is enabled
        // delay by 0.5s in case the player's outlaw data is null
        for (Player online : Bukkit.getOnlinePlayers()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    // grab the player's stored name
                    // convert it to a string
                    Object storedMemberName = plugin.getConfig().get(online.getUniqueId() + ".info.name");
                    String memberNameToString = storedMemberName.toString();

                    // make this player an outlaw if its set
                    if (plugin.getConfig().isSet(".outlaw.enabled")
                            && plugin.getConfig().getBoolean(online.getUniqueId() + ".outlaw.enabled", true)) {

                        nameTagChanger.changeNameGlobal(online, ChatColor.RED + memberNameToString);
                    } else {
                        nameTagChanger.changeNameGlobal(online, memberNameToString + "");
                    }
                }
            }.runTaskLater(plugin, 10);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {

        Player player = e.getPlayer();

        // grab the player's stored name
        // convert it to a string
        Object storedName = plugin.getConfig().get(player.getUniqueId() + ".info.name");
        String nameToString = storedName.toString();

        // set the player's name back to their stored name
        nameTagChanger.changeNameGlobal(player, nameToString);
    }
}
