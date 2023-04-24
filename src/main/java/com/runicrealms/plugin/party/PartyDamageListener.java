package com.runicrealms.plugin.party;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class PartyDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Party party = RunicCore.getPartyAPI().getParty(damager.getUniqueId());
            if (party != null && party.hasMember(victim)) {
                event.setCancelled(true);
            }
        }
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            if (shooter instanceof Player) {
                Player damager = (Player) shooter;
                Party party = RunicCore.getPartyAPI().getParty(damager.getUniqueId());
                if (party != null && party.hasMember(victim)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
