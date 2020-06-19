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
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) e.getEntity();
        if (e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Party party = RunicCore.getPartyManager().getPlayerParty(damager);
            if (party != null && party.hasMember(victim)) {
                e.setCancelled(true);
            }
        }
        if (e.getDamager() instanceof Projectile) {
            ProjectileSource shooter = (ProjectileSource) ((Projectile) e.getDamager()).getShooter();
            if (shooter instanceof Player) {
                Player damager = (Player) shooter;
                Party party = RunicCore.getPartyManager().getPlayerParty(damager);
                if (party != null && party.hasMember(victim)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
