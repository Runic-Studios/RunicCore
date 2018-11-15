package us.fortherealm.plugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class DurabilityListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDamage (PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }
}
