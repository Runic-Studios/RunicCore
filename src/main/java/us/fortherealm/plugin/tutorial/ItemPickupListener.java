package us.fortherealm.plugin.tutorial;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

/**
 * Prevents players from picking up items during the tutorial. (They shouldn't need to).
 */
public class ItemPickupListener implements Listener {

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {

        if (!(e.getEntity() instanceof Player)) return;

        Player pl = (Player) e.getEntity();

        if (!pl.hasPermission("tutorial.complete.1")) {
            e.setCancelled(true);
        }
    }
}
