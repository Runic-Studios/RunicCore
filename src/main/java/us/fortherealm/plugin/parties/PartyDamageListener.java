package us.fortherealm.plugin.parties;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import us.fortherealm.plugin.Main;

public class PartyDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player victim = (Player) e.getEntity();
            if (e.getDamager() instanceof Player) {
                Player damager = (Player) e.getDamager();
                Party party = Main.getPartyManager().getPlayerParty(damager);
                if (party != null && party.getMembers().contains(victim.getUniqueId())) {
                    e.setCancelled(true);
                }
            } else if (e.getDamager() instanceof Projectile) {
                ProjectileSource shooter = (ProjectileSource) ((Projectile) e.getDamager()).getShooter();
                if (shooter instanceof Player) {
                    Player damager = (Player) shooter;
                    Party party = Main.getPartyManager().getPlayerParty(damager);
                    if (party != null && party.getMembers().contains(victim.getUniqueId())) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
