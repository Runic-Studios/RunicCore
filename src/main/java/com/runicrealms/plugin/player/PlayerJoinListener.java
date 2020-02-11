package com.runicrealms.plugin.player;

import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterLoadEvent e) {

        Player pl = e.getPlayer();

        // set join message
        //e.setJoinMessage("");

        new BukkitRunnable() {
            @Override
            public void run() {

                // set their hp to stored value from last logout
                int storedHealth = e.getPlayerCache().getCurrentHealth();

                // new players or corrupted data
                if (storedHealth == 0) {
                    storedHealth = HealthUtils.getBaseHealth();
                }

                HealthUtils.setPlayerMaxHealth(pl);
                pl.setHealth(storedHealth);
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFirstJoin(CharacterLoadEvent event) {

        Player player = event.getPlayer();

        // setup for new players
        if (!player.hasPlayedBefore()) {

            // broadcast new player welcome message
            Bukkit.getServer().broadcastMessage(ChatColor.WHITE + player.getName()
                    + ChatColor.LIGHT_PURPLE + " joined the realm for the first time!");

            // setup hp
            HealthUtils.setBaseHealth(player);
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setFoodLevel(20);
        }
    }

    /**
     * Allows donator ranks to enter a full server
     */
    @EventHandler
    public void onJoinFullServer(PlayerLoginEvent e) {

        if (e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if (e.getPlayer().hasPermission("core.full.join")) {
                e.allow();
            }
        }
    }
}
