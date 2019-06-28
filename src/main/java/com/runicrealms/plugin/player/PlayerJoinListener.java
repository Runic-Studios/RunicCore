package com.runicrealms.plugin.player;

import com.runicrealms.plugin.player.utilities.HealthUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private RunicCore plugin = RunicCore.getInstance();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent e) {

        Player pl = e.getPlayer();

        // set join message
        // TODO: inform players if their guild mate or friend logs in.
        e.setJoinMessage("");

        // set their hp to stored value from last logout
        int storedHealth = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.currentHP");

        if (storedHealth == 0) {
            storedHealth = 50;
        }

        if (storedHealth > pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
            pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        } else {
            pl.setHealth(storedHealth);
        }

        // set the amount of hearts to display
        HealthUtils.setHeartDisplay(pl);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFirstJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // set the player's class to "None" if they don't have one setup (do this every login in case of corruption)
        if (!plugin.getConfig().isSet(uuid + ".info.class.name")) {
            setConfig(uuid, "class.name");
        }

        if (!plugin.getConfig().isSet(uuid + ".info.guild")) {
            setConfig(uuid, "guild");
        }

        if (!plugin.getConfig().isSet(uuid + ".info.prof.name")) {
            setConfig(uuid, "prof.name");
        }

        if (!plugin.getConfig().isSet(uuid + ".info.spellpoints")) {
            setConfig(uuid, "spellpoints", 0);
        }

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

    private void setConfig(UUID uuid, String setting) {
        RunicCore.getInstance().getConfig().set(uuid + ".info." + setting, "None");
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
    }

    private void setConfig(UUID uuid, String setting, int value) {
        RunicCore.getInstance().getConfig().set(uuid + ".info." + setting, value);
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
    }
}
