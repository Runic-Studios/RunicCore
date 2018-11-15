package us.fortherealm.plugin.listeners;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.outlaw.OutlawManager;
import java.util.UUID;

public class DeathEvent implements Listener {

    Plugin plugin = Main.getInstance();
    private OutlawManager outlawEvent = new OutlawManager();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerHitToDeath(EntityDamageByEntityEvent e) {

        // only listen if a player is the entity receiving damage AND that player "dies" (hp < 0)
        if (!(e.getEntity() instanceof Player && ((Player) e.getEntity()).getHealth() - e.getDamage() < 1)) {
            return;
        }

        // initialize event variables
        Player victim = (Player) e.getEntity();
        Entity damager = e.getDamager();
        Location respawnLocation = new Location(victim.getWorld(), -732, 34, 111);

        // if the player was killed by an arrow, set damager to its shooter
        if (damager instanceof Arrow) {
            Arrow arrow = (Arrow) damager;
            if (arrow.getShooter() instanceof Player) {
                damager = (Player) arrow.getShooter();
            }
        }

        // apply new death mechanics
        e.setCancelled(true);
        victim.setHealth(victim.getMaxHealth());
        victim.setFoodLevel(20);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 30,
                new Particle.DustOptions(Color.RED, 3));
        victim.teleport(respawnLocation);
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1);
        victim.sendMessage(ChatColor.RED + "You have been slain!");
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));

        // update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
            Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }

        // broadcast the death message
        broadcastSlainDeathMessage(damager, victim);

        // apply outlaw mechanics if the player is an outlaw AND the killer is an outlaw
        if (damager instanceof Player) {
            outlawEvent.onKill((Player) damager, victim);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent e) {

        // this event likes to get confused with the event above, so let's just fix that.
        if (e instanceof EntityDamageByEntityEvent) {
            return;
        }

        // only listen if a player is the entity receiving damage AND that player "dies" (hp < 0)
        if (!(e.getEntity() instanceof Player && ((Player) e.getEntity()).getHealth() - e.getDamage() < 1)) {
            return;
        }

        // initialize event variables
        Player victim = (Player) e.getEntity();
        Location respawnLocation = new Location(victim.getWorld(), -732, 34, 111);

        // apply new death mechanics
        victim.setHealth(victim.getMaxHealth());
        victim.setFoodLevel(20);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 30,
                new Particle.DustOptions(Color.RED, 3));
        victim.teleport(respawnLocation);
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1);
        victim.sendMessage(ChatColor.RED + "You have been slain!");
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));

        // update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
            Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }
    }

    private void broadcastSlainDeathMessage(Entity damager, Player victim) {

        // initialize method variables
        String nameDam = damager.getName();
        String nameVic = victim.getName();
        UUID p1 = damager.getUniqueId();
        UUID p2 = victim.getUniqueId();
        double ratingP1 = outlawEvent.getRating(p1);
        double ratingP2 = outlawEvent.getRating(p2);

        // if both players are outlaws, amend the death message to display their rating
        if (plugin.getConfig().getBoolean(p1 + ".outlaw.enabled", true)
                && plugin.getConfig().getBoolean(p2 + ".outlaw.enabled", true)) {
            nameDam = ChatColor.RED + "[" + (int) ratingP1 + "] " + ChatColor.WHITE + damager.getName();
            nameVic = ChatColor.RED + "[" + (int) ratingP2 + "] " + ChatColor.WHITE + victim.getName();
        }

        // display death message
        Bukkit.getServer().broadcastMessage(ChatColor.WHITE + nameVic + " was slain by " + nameDam);
    }

    private void broadcastDeathMessage(Player victim) {

        // initialize method variables
        String nameVic = victim.getName();

        // display death message
        Bukkit.getServer().broadcastMessage(ChatColor.WHITE + nameVic + " died!");
    }
}
