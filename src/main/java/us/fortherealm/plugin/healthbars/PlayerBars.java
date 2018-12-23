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
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;

import java.util.*;

// todo: make this work for spells
// todo: track target's health properly with a new EntityDamageEvent
public class PlayerBars implements Listener {

    private HashMap<UUID, BossBar> bossBarHashMap = new HashMap<>();
    //private HashMap<UUID, UUID> currentTarget = new HashMap<>();
    private HashMap<UUID, BukkitRunnable> currentRunnables = new HashMap<>();

    private BossBar bossBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
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

        // do nothing if they've died.
        if (health <= 0) return;

        // display the bar, add the player to our hashmap for later
        bossBar.addPlayer(damager);
        bossBarHashMap.put(damagerID, bossBar);
        String name = Main.getInstance().getConfig().get(victim.getUniqueId() + ".info.name").toString();
        bossBar.setProgress(health / victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        bossBar.setTitle(name + " " + ChatColor.RED + health + ChatColor.DARK_RED + " ❤");

//        if (currentTarget.containsValue(damagerID)) {
//            currentTarget.values().remove(damagerID);
//        }
        //currentTarget.put(victimID, damagerID);
//            if (currentRunnables.containsKey(damagerID)) {
//                currentRunnables.get(damagerID).cancel();
//                currentRunnables.remove(damagerID);
//            }

        // start the repeating task to remove the healthbar
        if (!currentRunnables.containsKey(damagerID)) {
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {

                    if (!Main.getCombatManager().getPlayersInCombat().containsKey(damagerID)) {
                        bossBarHashMap.get(damagerID).removePlayer(damager);
                        bossBarHashMap.remove(damagerID);
                        currentRunnables.remove(damagerID);
                        //currentTarget.remove(victimID);
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
}
