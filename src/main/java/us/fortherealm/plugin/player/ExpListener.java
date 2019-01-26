package us.fortherealm.plugin.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Disables vanilla experience drops.
 */
public class ExpListener implements Listener {

    @EventHandler
    public void onExpDrop(EntityDeathEvent e) {
        e.setDroppedExp(0);
    }
}
