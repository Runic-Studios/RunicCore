package us.fortherealm.plugin.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import us.fortherealm.plugin.FTRCore;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit (PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // remove leave message
        event.setQuitMessage("");

        // make sure the player's walk speed is reset
        player.setWalkSpeed(0.2f);

        // save player hp
        FTRCore.getInstance().getConfig().set(player.getUniqueId() + ".info.currentHP", (int) player.getHealth());
        FTRCore.getInstance().saveConfig();
        FTRCore.getInstance().reloadConfig();
    }
}
