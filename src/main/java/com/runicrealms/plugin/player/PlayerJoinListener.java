package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.runiccharacters.api.events.CharacterLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

    // todo: make player nameplates invisible w/ scoreboard teams?
    /**
     * Reset the player's displayed values when they join the server, before selecting a character
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        Player pl = e.getPlayer();
        // build database file async (if it doesn't exist)
        Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(),
                () -> RunicCore.getCacheManager().tryCreateNewPlayer(pl), 1L);
        pl.getInventory().clear();
        pl.setInvulnerable(true);
        pl.setMaxHealth(20);
        pl.setHealth(pl.getMaxHealth());
        pl.setHealthScale(20);
        pl.setLevel(0);
        pl.setExp(0);
        pl.setFoodLevel(20);
        pl.teleport(new Location(Bukkit.getWorld("Alterra"), -2318.5, 2, 1720.5));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterLoadEvent e) {

        Player pl = e.getPlayer();
        pl.setInvulnerable(false);

        new BukkitRunnable() {
            @Override
            public void run() {

                // set their inventory
                pl.getInventory().setContents(e.getPlayerCache().getInventoryContents());
                pl.updateInventory();

                HealthUtils.setPlayerMaxHealth(pl);
                HealthUtils.setHeartDisplay(pl);

                // update player's level (this will change storedHealth, but we alrdy got variable hehe)
                pl.setLevel(e.getPlayerCache().getClassLevel());
                int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(e.getPlayerCache().getClassLevel());
                int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(e.getPlayerCache().getClassLevel()+1);
                double proportion = (double) (e.getPlayerCache().getClassExp() - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
                if (e.getPlayerCache().getClassLevel() >= PlayerLevelUtil.getMaxLevel()) pl.setExp(0);
                if (proportion < 0) proportion = 0.0f;
                pl.setExp((float) proportion);

                // set their location
                pl.teleport(e.getPlayerCache().getLocation());

                // prompt resource pack
                pl.setResourcePack("https://www.dropbox.com/s/9ymuk315d59gif1/RR%20Official%20Pack.zip?dl=1");
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);

        new BukkitRunnable() {
            @Override
            public void run() {

                // set their hp to stored value from last logout
                int storedHealth = e.getPlayerCache().getCurrentHealth();

                // update their health
                // new players or corrupted data
                if (storedHealth == 0) {
                    storedHealth = HealthUtils.getBaseHealth();
                }
                if (storedHealth <= pl.getMaxHealth()) {
                    pl.setHealth(storedHealth);
                } else {
                    pl.setHealth(pl.getMaxHealth());
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFirstLoad(CharacterLoadEvent event) {

        Player pl = event.getPlayer();

        // setup for new players
        if (!pl.hasPlayedBefore()) {

            // broadcast new player welcome message
            Bukkit.getServer().broadcastMessage(ChatColor.WHITE + pl.getName()
                    + ChatColor.LIGHT_PURPLE + " joined the realm for the first time!");

            // heal player
            HealthUtils.setPlayerMaxHealth(pl);
            HealthUtils.setHeartDisplay(pl);
            int playerHealth = (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            pl.setHealth(playerHealth);
            pl.setFoodLevel(20);
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
