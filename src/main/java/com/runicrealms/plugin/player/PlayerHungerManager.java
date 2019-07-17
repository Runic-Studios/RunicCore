package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created by KissOfFate
 * Date: 6/26/2019
 * Time: 2:19 PM
 */
@SuppressWarnings("FieldCanBeLocal")
public class PlayerHungerManager implements Listener {

    // tick time in seconds
    private final int PLAYER_HUNGER_TIME = 90;

    public PlayerHungerManager() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("runic.hunger.exempt")
                            || (player.getGameMode() != GameMode.CREATIVE
                            && player.getGameMode() != GameMode.SPECTATOR)) {

                        if (player.getFoodLevel() <= 1) return;

                        player.setFoodLevel(player.getFoodLevel() - 1);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 30 * 20, PLAYER_HUNGER_TIME * 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(event.getFoodLevel() < player.getFoodLevel()) {
                if (!player.hasPermission("runic.hunger.exempt")) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
