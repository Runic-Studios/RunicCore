package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("deprecation")
public class PlayerJoinListener implements Listener {

    /**
     * Reset the player's displayed values when they join the server, before selecting a character
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        Player pl = e.getPlayer();
        pl.setMaxHealth(20);
        pl.setHealth(pl.getMaxHealth());
        pl.setHealthScale(20);
        pl.setLevel(0);
        pl.setExp(0);
        pl.setFoodLevel(20);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterLoadEvent e) {

        Player pl = e.getPlayer();

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

                // update player's level
                pl.setLevel(e.getPlayerCache().getClassLevel());
                int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(e.getPlayerCache().getClassLevel());
                int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(e.getPlayerCache().getClassLevel()+1);
                double proportion = (double) (e.getPlayerCache().getClassExp() - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
                if (e.getPlayerCache().getClassLevel() >= PlayerLevelUtil.getMaxLevel()) pl.setExp(0);
                if (proportion < 0) proportion = 0.0f;
                pl.setExp((float) proportion);
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
