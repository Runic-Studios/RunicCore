package us.fortherealm.plugin.listeners;

import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.util.ScoreboardUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ScoreboardListener implements Listener {

    ScoreboardUtil boardUtil = new ScoreboardUtil();
    private Main plugin = Main.getInstance();

    // *** ON PLAYER LOGIN, UPDATES ALL HEALTH BAR SCORES TO THEIR RESPECTIVE PLAYER'S CURRENT HP *** //
    // *** MOST OF THE CODE FOR THIS IS IN ITEMS MAIN or SCOREBOARD UTIL BTW *** //
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {


        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                Player pl = e.getPlayer();
                boardUtil.setupScoreboard(pl);
            }
        },20);//==1.0s (20 ticks so as to wait for the health to be updated from 20 ==> 50 first)
    }
}