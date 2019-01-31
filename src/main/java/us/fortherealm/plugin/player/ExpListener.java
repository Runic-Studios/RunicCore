package us.fortherealm.plugin.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

/**
 * Disables vanilla experience drops.
 * Also disables players from gaining experience at max level.
 */
public class ExpListener implements Listener {

    @EventHandler
    public void onExpDrop(EntityDeathEvent e) {
        e.setDroppedExp(0);
    }

    @EventHandler
    public void onExpGain(PlayerExpChangeEvent e) {
        if (e.getPlayer().getLevel() >= 50) {
            e.setAmount(0);
        }
    }
}
