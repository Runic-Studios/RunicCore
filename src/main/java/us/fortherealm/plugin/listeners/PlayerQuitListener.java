package us.fortherealm.plugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit (PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // remove leave message
        event.setQuitMessage("");

        // make sure the player's walk speed is reset
        player.setWalkSpeed(0.2f);
    }
}
