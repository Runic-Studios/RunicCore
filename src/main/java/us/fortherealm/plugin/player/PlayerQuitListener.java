package us.fortherealm.plugin.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import us.fortherealm.plugin.Main;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit (PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // remove leave message
        event.setQuitMessage("");

        // make sure the player's walk speed is reset
        player.setWalkSpeed(0.2f);

        // save player hp
        Main.getInstance().getConfig().set(player.getUniqueId() + ".info.health", player.getHealth());
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
    }
}
