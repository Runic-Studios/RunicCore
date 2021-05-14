package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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
    private final int PLAYER_HUNGER_TIME = 60;

    public PlayerHungerManager() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : RunicCore.getCacheManager().getLoadedPlayers()) {
                    if (RunicCoreAPI.isSafezone(player.getLocation())) { // prevent hunger loss in capital cities
                        if (player.getFoodLevel() < 20) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                            player.sendMessage(ChatColor.GREEN + "You feel safe within the city! Your hunger has been restored.");
                            player.setFoodLevel(20);
                        }
                        continue;
                    }
                    if (player.getFoodLevel() <= 1) continue;
                    player.setFoodLevel(player.getFoodLevel() - 1);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 30 * 20, PLAYER_HUNGER_TIME * 20L);
    }

    /**
     * Prevents normal decay of hunger
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
            }
        }
    }
}
