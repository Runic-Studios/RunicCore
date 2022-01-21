package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.resourcepack.ResourcePackManager;
import com.runicrealms.runicnpcs.api.RunicNpcsAPI;
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

@SuppressWarnings("deprecation")
public class PlayerJoinListener implements Listener {

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
        // prompt resource pack
        ResourcePackManager.openPackForPlayer(pl);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterLoadEvent e) {
        Player player = e.getPlayer();
        PlayerCache playerCache = e.getPlayerCache();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> loadPlayerHealthScaleLevelAndLocation(player, playerCache), 1L);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> loadCurrentPlayerHealthAndHunger(player, playerCache), 2L);
    }

    /**
     * Sets up some basic player values, such as max health, level, location, etc.
     *
     * @param player      to set values for
     * @param playerCache the stored object with values
     */
    private void loadPlayerHealthScaleLevelAndLocation(Player player, PlayerCache playerCache) {
        player.setInvulnerable(false);
        HealthUtils.setPlayerMaxHealth(player);
        player.setHealthScale(HealthUtils.getHeartAmount());
        player.setLevel(playerCache.getClassLevel()); // update player's level (this will change storedHealth, but we already got variable)
        int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(playerCache.getClassLevel());
        int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(playerCache.getClassLevel() + 1);
        double proportion = (double) (playerCache.getClassExp() - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
        if (playerCache.getClassLevel() >= PlayerLevelUtil.getMaxLevel()) player.setExp(0);
        if (proportion < 0) proportion = 0.0f;
        if (proportion >= 1) proportion = 0.99f;
        player.setExp((float) proportion);
        player.teleport(playerCache.getLocation()); // set their location
    }

    /**
     * Loads the current player values associated w/ combat, like current health, hunger, etc.
     *
     * @param player      to set values for
     * @param playerCache the stored object with values
     */
    private void loadCurrentPlayerHealthAndHunger(Player player, PlayerCache playerCache) {
        // set their hp to stored value from last logout
        int storedHealth = playerCache.getCurrentHealth();
        // update their health
        // new players or corrupted data
        if (storedHealth == 0) {
            storedHealth = HealthUtils.getBaseHealth();
        }
        if (storedHealth <= player.getMaxHealth()) {
            player.setHealth(storedHealth);
        } else {
            player.setHealth(player.getMaxHealth());
        }
        // set their last stored hunger
        player.setFoodLevel(playerCache.getStoredHunger());
    }

    /**
     * Setup for new players
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFirstLoad(CharacterLoadEvent event) {
        Player pl = event.getPlayer();
        if (pl.hasPlayedBefore()) return;
        // broadcast new player welcome message
        Bukkit.getServer().broadcastMessage(ChatColor.WHITE + pl.getName()
                + ChatColor.LIGHT_PURPLE + " joined the realm for the first time!");
        // heal player
        HealthUtils.setPlayerMaxHealth(pl);
        pl.setHealthScale(HealthUtils.getHeartAmount());
        int playerHealth = (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        pl.setHealth(playerHealth);
        pl.setFoodLevel(20);
    }

    // Handles loading in Runic NPCs on player login
    // Loads with delay to allow for data loading in NPCs plugin
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoadHandleNPCs(CharacterLoadEvent event) {
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> RunicNpcsAPI.updateNpcsForPlayer(event.getPlayer()), 5L);
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
