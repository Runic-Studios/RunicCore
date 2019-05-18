package com.runicrealms.plugin.healthbars;

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
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

import java.util.*;

/**
 * This class manages player vs. player health bars using Minecraft's boss bar system.
 */
public class PlayerBars implements Listener {

    private HashMap<UUID, BossBar> bossBarHashMap = new HashMap<>();
    private HashMap<UUID, UUID> currentTarget = new HashMap<>();
    private HashMap<UUID, BukkitRunnable> currentRunnables = new HashMap<>();

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent e) {

        // only listen for two players, or arrows
        if (!(e.getDamager() instanceof Player) && !(e.getDamager() instanceof Arrow)) return;
        if (!(e.getEntity() instanceof Player)) return;

        // ignore NPCs
        if (e.getEntity().hasMetadata("NPC")) return;

        // grab our variables
        Player damager;
        if (e.getDamager() instanceof Arrow) {
            damager = (Player) ((Arrow) e.getDamager()).getShooter();
        } else {
            damager = (Player) e.getDamager();
        }
        Player victim = (Player) e.getEntity();

        // ignore party members
        if (RunicCore.getPartyManager().getPlayerParty(damager) != null
                && RunicCore.getPartyManager().getPlayerParty(damager).hasMember(victim)) {
            return;
        }

        // player can't enter combat with themselves
        if (damager == victim) return;

        UUID damagerID = damager.getUniqueId();
        UUID victimID = victim.getUniqueId();

        // set bar's display to enemy's health
        double health = victim.getHealth() - e.getDamage();

        if (health <= 0) return;

        // -------------------------------------------------------------------------
        // display the bar, add the player to our hashmap so we can remove 'em later, set the progress, title
        BossBar bossBar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
        //String name = RunicCore.getInstance().getConfig().get(victim.getUniqueId() + ".info.name").toString();
        String name = victim.getName();
        bossBar.setProgress(health / victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        bossBar.setTitle(name + " " + ChatColor.RED + (int) (health) + ChatColor.DARK_RED + " ❤");

        // remove the player from the map to prevent duplicate health bars
        if (bossBarHashMap.containsKey(damagerID)) {
            bossBarHashMap.get(damagerID).removePlayer(damager);
        }

        bossBar.addPlayer(damager);
        bossBarHashMap.put(damagerID, bossBar);
        // -------------------------------------------------------------------------

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

                    if (!RunicCore.getCombatManager().getPlayersInCombat().containsKey(damagerID)) {
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
            runnable.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20);

            // prevent multiple runnables from happening
            currentRunnables.put(damagerID, runnable);
        }
    }

    /**
     * This event updates the victim's healthbar for his attacker if he takes damage from a source
     * OTHER than the attacker.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {

        // retrieve the player victim
        if (!(e.getEntity() instanceof Player)) return;

        // ignore NPCs
        if (e.getEntity().hasMetadata("NPC")) return;

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
            BossBar bossBar = bossBarHashMap.get(attackerID);
            double health = victim.getHealth() - e.getDamage();

            if (health <= 0) return;

            //String name = RunicCore.getInstance().getConfig().get(victim.getUniqueId() + ".info.name").toString();
            String name = victim.getName();

            // if the bossbar isn't null, update the victim's health for the attacker
            if (bossBar != null) {
                bossBar.setProgress(health / victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                bossBar.setTitle(name + " " + ChatColor.RED + (int) Math.ceil(health) + ChatColor.DARK_RED + " ❤");
            }
        }
    }

    /**
     * This event updates the victim's healthbar for his attacker if he regains health
     * (Delayed by 1 tick to show the proper health value)
     */
    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent e) {

        // retrieve the player victim
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!(e.getEntity() instanceof Player)) return;
                Player victim = (Player) e.getEntity();
                UUID victimID = victim.getUniqueId();

                if (currentTarget.containsKey(victimID)) {

                    // retrieve the player attacker
                    Player attacker = Bukkit.getPlayer(currentTarget.get(victimID));
                    UUID attackerID = attacker.getUniqueId();

                    // retrieve the damager's bossbar and variables
                    BossBar bossBar = bossBarHashMap.get(attackerID);
                    double health = victim.getHealth();

                    if (health <= 0) return;

                    //String name = RunicCore.getInstance().getConfig().get(victim.getUniqueId() + ".info.name").toString();
                    String name = victim.getName();

                    // if the bossbar isn't null, update the victim's health for the attacker
                    if (bossBar != null) {
                        bossBar.setProgress(health / victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
                        bossBar.setTitle(name + " " + ChatColor.RED + (int) Math.ceil(health) + ChatColor.DARK_RED + " ❤");
                    }
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 1);
    }
}
