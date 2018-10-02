package us.fortherealm.plugin.scoreboard;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;

public class ScoreboardListener implements Listener {

    private ScoreboardManager sbm = new ScoreboardManager();
    private Plugin plugin = Main.getInstance();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();

        // delay by 1s on first join to wait for server to change hp from 20 ==> 50
        if (!player.hasPlayedBefore()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    sbm.setupScoreboard(e.getPlayer());
                }
            }.runTaskLater(plugin, 20);

        // otherwise update quickly
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    sbm.setupScoreboard(e.getPlayer());
                }
            }.runTaskLater(plugin, 1);
        }
    }
}