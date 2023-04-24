package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * This event is used to prevent execution of vanilla-style or ACF commands
 * until the player has fully loaded
 *
 * @author Skyfallin
 */
public class PreCommandListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Check if the player executing the command is in the list of loaded players
        if (!RunicCore.getCharacterAPI().getLoadedCharacters().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot execute commands before your character is loaded!");
        }
    }

}
