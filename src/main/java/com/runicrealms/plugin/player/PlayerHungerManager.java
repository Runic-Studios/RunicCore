package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerHungerManager implements Listener {

    private static final int HUNGER_TICK_TASK_DELAY = 60; // seconds
    private static final int PLAYER_HUNGER_TIME = 60; // tick time in seconds
    private static final int INVIGORATED_HUNGER_THRESHOLD = 17; // hunger level to receive regen
    private static final int STARVATION_HUNGER_LEVEL = 1;

    public PlayerHungerManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(RunicCore.getInstance(),
                this::tickAllOnlinePlayersHunger, HUNGER_TICK_TASK_DELAY * 20L, PLAYER_HUNGER_TIME * 20L);
    }

    /**
     * Manually reduce player hunger
     */
    private void tickAllOnlinePlayersHunger() {
        for (Player player : RunicCore.getCacheManager().getLoadedPlayers()) {
            if (RunicCoreAPI.isSafezone(player.getLocation())) { // prevent hunger loss in capital cities
                if (player.getFoodLevel() < 20) {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    player.sendMessage(ChatColor.GREEN + "You feel safe within the city! Your hunger has been restored.");
                    player.setFoodLevel(20);
                }
                continue;
            }
            if (player.getFoodLevel() <= STARVATION_HUNGER_LEVEL) continue;
            player.setFoodLevel(player.getFoodLevel() - 1);
        }
    }

    /**
     * Prevents normal decay of hunger
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (event.getFoodLevel() <= player.getFoodLevel()) {
            event.setCancelled(true);
        } else {
            if (event.getFoodLevel() >= INVIGORATED_HUNGER_THRESHOLD && player.getFoodLevel() < INVIGORATED_HUNGER_THRESHOLD) { // no duplicate messages
                player.sendMessage
                        (
                                ChatColor.YELLOW + "You are now " +
                                        ChatColor.YELLOW + ChatColor.ITALIC +
                                        "invigorated" + ChatColor.YELLOW +
                                        ", regenerating health over time."
                        );
            }
        }
    }

    /**
     * Prevent eating items which are not consumables
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onFoodInteract(PlayerItemConsumeEvent e) {
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(e.getItem());
        if (runicItem == null) return;
        boolean isConsumable = runicItem.getTags().contains(RunicItemTag.CONSUMABLE);
        if (!isConsumable) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "I can't eat that!");
        }
    }

    public static int getInvigoratedHungerThreshold() {
        return INVIGORATED_HUNGER_THRESHOLD;
    }
}
