package us.fortherealm.plugin.healthbars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;

import java.util.*;

public class PlayerBars implements Listener {

    private static HashMap<UUID, BossBar> bossBarHashMap = new HashMap<>();
    private HashMap<UUID, UUID> currentTarget = new HashMap<>();
    private static HashMap<UUID, BukkitRunnable> currentRunnables = new HashMap<>();
    private BossBar bossBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);

    public static HashMap<UUID, BossBar> getBossBars() {
        return bossBarHashMap;
    }
    public static HashMap<UUID, BukkitRunnable> getCurrentRunnables() { return currentRunnables; }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent e) {

        // only listen for two players, or arrows
        if (!(e.getDamager() instanceof Player) && !(e.getDamager() instanceof Arrow)) return;
        if (!(e.getEntity() instanceof Player)) return;

        // grab our variables
        Player damager;
        if (e.getDamager() instanceof Arrow) {
            damager = (Player) ((Arrow) e.getDamager()).getShooter();
        } else {
            damager = (Player) e.getDamager();
        }

        Player victim = (Player) e.getEntity();
        UUID damagerID = damager.getUniqueId();
        UUID victimID = victim.getUniqueId();

        // prevent duplicates
        if (bossBar.getPlayers().contains(damager)) {
            bossBar.removePlayer(damager);
        }

        // set bar's display to enemy's health
        double health = victim.getHealth() - e.getDamage();

        if (health <= 0) return;

        // display the bar, add the player to our hashmap for later
        bossBar.addPlayer(damager);
        bossBarHashMap.put(damagerID, bossBar);
        String name = Main.getInstance().getConfig().get(victim.getUniqueId() + ".info.name").toString();

        // set the progress, title
        bossBar.setProgress(health / victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        bossBar.setTitle(name + " " + ChatColor.RED + (int) health + ChatColor.DARK_RED + " ❤");

        // set the victim to the damager's current target (so their health can be tracked by the method below)
        if (currentTarget.containsValue(damagerID)) {
            currentTarget.values().remove(damagerID);
        }
        currentTarget.put(victimID, damagerID);

//        if (currentRunnables.containsKey(damagerID)) {
//            currentRunnables.get(damagerID).cancel();
//            currentRunnables.remove(damagerID);
//        }

        // start the repeating task to remove the healthbar
        if (!currentRunnables.containsKey(damagerID)) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {

                    if (!Main.getCombatManager().getPlayersInCombat().containsKey(damagerID)) {
                        if (bossBarHashMap.containsKey(damagerID)) {
                            bossBarHashMap.get(damagerID).removePlayer(damager);
                            bossBarHashMap.remove(damagerID);
                        }
                        currentRunnables.remove(damagerID);
                        currentTarget.remove(victimID);
                        this.cancel();
                    }
                }
            };
            runnable.runTaskTimerAsynchronously(Main.getInstance(), 0, 20);

            // prevent multiple runnables from happening
            currentRunnables.put(damagerID, runnable);
        }

        // inform the players when they first enter combat
        if (!Main.getCombatManager().getPlayersInCombat().containsKey(damagerID)) {
            damager.sendMessage(ChatColor.RED + "You have entered combat!");
        }
        if (!Main.getCombatManager().getPlayersInCombat().containsKey(victimID)) {
            victim.sendMessage(ChatColor.RED + "You have entered combat!");
        }

        // add/refresh their combat timer every hit
        Main.getCombatManager().addPlayer(damagerID, System.currentTimeMillis());
        Main.getCombatManager().addPlayer(victimID, System.currentTimeMillis());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {

        // retrieve the player victim
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        UUID victimID = victim.getUniqueId();

        if (currentTarget.containsKey(victimID)) {

            // retrieve the player attacker
            Player attacker = Bukkit.getPlayer(currentTarget.get(victimID));
            UUID attackerID = attacker.getUniqueId();

            // ignore this if the attacker is causing the damage, since the above method handles that
            // also listen for the attacker's arrows.
            if (e instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent ed = (EntityDamageByEntityEvent) e;
                    if (ed.getDamager().getUniqueId().equals(attackerID)) {
                        return;
                    }
                    if (ed.getDamager() instanceof Arrow && ((Arrow) ed.getDamager()).getShooter() instanceof Player) {
                        if (((Player) ((Arrow) ed.getDamager()).getShooter()).getUniqueId().equals(attackerID)) {
                            return;
                        }
                    }
            }

            // retrieve the damager's bossbar and variables
            BossBar bossBar = bossBarHashMap.get(attacker.getUniqueId());
            double health = victim.getHealth() - e.getDamage();

            if (health <= 0) return;

            String name = Main.getInstance().getConfig().get(victim.getUniqueId() + ".info.name").toString();

            // if the bossbar isn't null, update the victim's health for the attacker
            if (bossBar != null) {
                bossBar.setProgress(health / victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                bossBar.setTitle(name + " " + ChatColor.RED + (int) health + ChatColor.DARK_RED + " ❤");
            }
        }
    }
}
