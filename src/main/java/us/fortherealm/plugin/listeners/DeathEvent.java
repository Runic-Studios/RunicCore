package us.fortherealm.plugin.listeners;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
// TODO: Check if killer/victim are in OUTLAW MODE
// TODO: Apply ELO ratings if OUTLAW MODE
// TODO: Add ELO ratings to death messages
// TODO: change death method to its own method, add checks for types of death
public class DeathEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerHitToDeath(EntityDamageByEntityEvent event) {
        Entity damagedEntity = event.getEntity();
        Location respawnLocation = new Location(damagedEntity.getWorld(), -732, 34, 111);
        World world = damagedEntity.getWorld();
        if (damagedEntity instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (victim.getHealth() - event.getDamage() < 1) {
                world.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
                victim.getWorld().spigot().playEffect(victim.getEyeLocation(), Effect.COLOURED_DUST, 0, 0, 0.2F, 0.2F, 0.2F, 0.00F, 30, 16);
                event.setCancelled(true);
                victim.setHealth(victim.getMaxHealth());
                victim.setFoodLevel(20);
                victim.teleport(respawnLocation);
                broadcastDeathMessage(victim, event.getDamager());
                victim.sendMessage(ChatColor.RED + "You have been slain!");
                victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
                if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
                    Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
                    Score score = o.getScore(victim);
                    score.setScore((int) victim.getHealth());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            return;
        }
        Entity damagedEntity = event.getEntity();
        Location respawnLocation = new Location(damagedEntity.getWorld(), -732, 34, 111);
        World world = damagedEntity.getWorld();
        if (damagedEntity instanceof Player) {
            Player victim = (Player) event.getEntity();
            if (victim.getHealth() - event.getDamage() < 1) {
                event.setCancelled(true);
                victim.setHealth(victim.getMaxHealth());
                victim.setFoodLevel(20);
                victim.setFireTicks(0);
                world.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
                victim.getWorld().spigot().playEffect(victim.getEyeLocation(), Effect.COLOURED_DUST, 0, 0, 0.2F, 0.2F, 0.2F, 0.00F, 30, 16);
                victim.teleport(respawnLocation);
                victim.sendMessage(ChatColor.RED + "Oh no, you have died!");
                victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1);
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
                if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
                    Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
                    Score score = o.getScore(victim);
                    score.setScore((int) victim.getHealth());
                }
            }
        }
    }

    public void broadcastDeathMessage(Player victim, Entity damageSource) {
        if (damageSource instanceof Arrow) {
            Arrow arrow = (Arrow) damageSource;
            if (arrow.getShooter() instanceof Player) {
                Bukkit.getServer().broadcastMessage(ChatColor.WHITE + victim.getName() + " was slain by " + ((Player) arrow.getShooter()).getName());
            }
        } else if (damageSource instanceof SmallFireball) {
            SmallFireball smfb = (SmallFireball) damageSource;
            if (smfb.getShooter() instanceof Player) {
                Bukkit.getServer().broadcastMessage(ChatColor.WHITE + victim.getName() + " was slain by " + ((Player) smfb.getShooter()).getName());
            }
        } else {
            Bukkit.getServer().broadcastMessage(ChatColor.WHITE + victim.getName() + " was slain by " + damageSource.getName());
        }
    }
}
