package us.fortherealm.plugin.player;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.player.utilities.HealthUtils;

public class PlayerJoinListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        Player pl = e.getPlayer();

        // set their hp to stored value from last logout
        // set their health scale
        HealthUtils.setHeartDisplay(pl);
        int storedHealth = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.currentHP");

        if (storedHealth == 0) {
            storedHealth = 50;
        }

        if (storedHealth > pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
            pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        } else {
            pl.setHealth(storedHealth);
        }
    }
}
