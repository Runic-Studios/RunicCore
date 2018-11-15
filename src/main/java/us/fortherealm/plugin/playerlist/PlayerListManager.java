package us.fortherealm.plugin.playerlist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kacperduras.protocoltab.ProtocolTabAPI;
import pl.kacperduras.protocoltab.manager.ProtocolTab;
import us.fortherealm.plugin.Main;

import java.util.ArrayList;

public class PlayerListManager implements Listener {

    private Plugin plugin = Main.getInstance();
    private int onlineCount;
    private int guildCount;
    private int friendCount;

    // create an array of strings to be used later
    private ArrayList<String> onlinePlayers = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();

        // fill the entire player list with blank slots
        // -------------------------------------------------------------
        // delay by 1 tick to make sure the player's tablist is not null
        // -------------------------------------------------------------
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int j = 0; j < 80; j++) {
                    ProtocolTabAPI.getTablist(player).setSlot(j, ProtocolTab.BLANK_TEXT);
                }

                // grab the joiner's stored name, convert it to a string
                String storedName = plugin.getConfig().get(player.getUniqueId() + ".info.name").toString();

                //boolean contains = Arrays.stream(otherNames).anyMatch("s"::equals);

                // add the player's stored name to the online list, check for duplicates
                if (!onlinePlayers.contains(storedName)) {
                    onlinePlayers.add(storedName);
                }

                // update joiner and other online players' scoreboards
                for (Player online : Bukkit.getOnlinePlayers()) {
                    for (int k = 0; k < onlinePlayers.size() && k < 20; k++) {
                        if (onlinePlayers.get(k) != null) {
                            ProtocolTabAPI.getTablist(online).setSlot(k+1, onlinePlayers.get(k));
                        }
                    }
                }

                // set the player list header and footer
                ProtocolTabAPI.getTablist(player).setHeader("&d&lFor The Realm &c&lAlpha\n&7Info for: &f" + storedName);
                ProtocolTabAPI.getTablist(player).setFooter("&cwww.fortherealm.us");

                // party count will always be 0 on login, so we can just set that here.
                ProtocolTabAPI.getTablist(player).setSlot(40, "  &a&n Party (" + 0 + ") &r");

                for (Player online : Bukkit.getOnlinePlayers()) {
                    onlineCount = Bukkit.getOnlinePlayers().size();
                    ProtocolTabAPI.getTablist(online).setSlot(0, "  &7&n Online (" + onlineCount + ") &r");
                    ProtocolTabAPI.getTablist(online).setSlot(20, "  &6&n Guild (" + guildCount + ") &r");
                    ProtocolTabAPI.getTablist(online).setSlot(60, "  &9&n Friends (" + friendCount + ") &r");
                    ProtocolTabAPI.getTablist(online).update();
                }
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        removeFromPlayerList(e.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e){
        removeFromPlayerList(e.getPlayer());
    }

    private void removeFromPlayerList(Player player) {

        // remove the leaver from the online array
        String storedName = plugin.getConfig().get(player.getUniqueId() + ".info.name").toString();
        onlinePlayers.remove(storedName);


        // update leaver and other online players' player lists
        for (Player online : Bukkit.getOnlinePlayers()) {

            // subtract one because the event counts the leaver
            onlineCount = Bukkit.getOnlinePlayers().size()-1;
            ProtocolTabAPI.getTablist(online).setSlot(0, "  &7&n Online (" + (onlineCount) + ") &r");
            ProtocolTabAPI.getTablist(online).setSlot(20, "  &6&n Guild (" + guildCount + ") &r");
            ProtocolTabAPI.getTablist(online).setSlot(60, "  &9&n Friends (" + friendCount + ") &r");

            // reset the online column
            for (int j = 1; j < 20; j++) {
                ProtocolTabAPI.getTablist(online).setSlot(j, ProtocolTab.BLANK_TEXT);
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    // fill it with the new
                    for (int k = 0; k < onlinePlayers.size() && k < 20; k++) {
                        if (onlinePlayers.get(k) != null) {
                            ProtocolTabAPI.getTablist(online).setSlot(k+1, onlinePlayers.get(k));
                            ProtocolTabAPI.getTablist(online).update();
                        }
                    }
                }
            }.runTaskLater(plugin, 1L); // delayed by one tick for reasons.
        }
    }

    public ArrayList<String> getOnlinePlayers() {
        return this.onlinePlayers;
    }
}
