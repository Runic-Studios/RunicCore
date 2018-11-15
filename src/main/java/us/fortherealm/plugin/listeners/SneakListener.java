package us.fortherealm.plugin.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SneakListener implements Listener {

    // cancel sneaking while player is holding artifact
    public void onSneak(PlayerToggleSneakEvent e) {

        if (e.getPlayer().isSneaking()) {
            e.setCancelled(true);
        }
    }
}
