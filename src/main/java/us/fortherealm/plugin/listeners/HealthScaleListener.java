package us.fortherealm.plugin.listeners;

import org.bukkit.attribute.Attribute;
import us.fortherealm.plugin.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class HealthScaleListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player pl = e.getPlayer();
        double maxHealth = pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        //ex: (50 / 12.5) = 4.0 = 2 hearts displayed
        pl.setHealthScale(maxHealth / 12.5);
    }
}
