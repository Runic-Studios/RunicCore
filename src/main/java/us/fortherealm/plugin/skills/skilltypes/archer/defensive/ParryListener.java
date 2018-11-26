package us.fortherealm.plugin.skills.skilltypes.archer.defensive;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ParryListener implements Listener {

    private Parry parry = new Parry();

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            UUID uuid = e.getEntity().getUniqueId();
            if (parry.getNoFall().containsKey(uuid)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        parry.getNoFall().remove(uuid);
    }
}
