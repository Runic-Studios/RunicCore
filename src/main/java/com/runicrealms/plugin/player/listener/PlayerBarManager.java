package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.player.bar.PlayerBar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class PlayerBarManager implements Listener {
    // Store the armor stand for each player in HashMaps
    HashMap<Player, PlayerBar> playerBars = new HashMap<>();

    public PlayerBarManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        startPlayerBarTask();
    }

    @EventHandler
    public void onGenericDamage(GenericDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) return;
        updatePlayerBar(player);
    }

    @EventHandler
    public void onLoad(CharacterLoadedEvent event) {
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            PlayerBar playerBar = new PlayerBar(event.getPlayer());
            playerBar.attachPlayerBar(playerBars);
        });
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        updatePlayerBar(event.getPlayer());
    }

    @EventHandler
    public void onMobDamage(MobDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) return;
        updatePlayerBar(player);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        updatePlayerBar(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerBars.get(player) != null) {
            removeAllPassengers(player);
        }
        playerBars.remove(player);
    }

    private void removeAllPassengers(Player player) {
        // Remove all passengers and nested passengers
        player.getPassengers().forEach(passenger -> {
            passenger.getPassengers().forEach(Entity::remove);
            passenger.remove();
        });
    }

    private void startPlayerBarTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : playerBars.keySet()) {
                    if (!player.isOnline()) continue;
                    if (player.getPassengers().size() < 1) { // Something got dismounted (swimming)
                        // Remove the entities
                        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> playerBars.get(player).clearPlayerBar());
                        if (!player.isSwimming()) {
                            // The player is not swimming, create a new Parrot and ArmorStand and reattach them
                            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> playerBars.get(player).refreshPlayerBar(playerBars));
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 40L);
    }

    private void updatePlayerBar(Player player) {
        PlayerBar playerBar = playerBars.get(player);
        playerBar.getArmorStand().setCustomName((int) player.getHealth() + "" + ChatColor.RED + " ❤");
    }

}
