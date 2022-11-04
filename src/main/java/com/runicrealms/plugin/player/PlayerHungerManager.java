package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.HealthRegenEvent;
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

import java.util.UUID;

public class PlayerHungerManager implements Listener {

    private static final int HUNGER_TICK_TASK_DELAY = 60; // seconds
    private static final int PLAYER_HUNGER_TIME = 60; // tick time in seconds
    private static final int INVIGORATED_HUNGER_THRESHOLD = 6; // hunger level to receive regen
    private static final int STARVATION_HUNGER_LEVEL = 1;
    private static final double HALF_HUNGER_REGEN_MULTIPLIER = 0.5;

    public PlayerHungerManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(),
                this::tickAllOnlinePlayersHunger, HUNGER_TICK_TASK_DELAY * 20L, PLAYER_HUNGER_TIME * 20L);
    }

    /**
     * Manually reduce player hunger. Either reduces player saturation if it exists,
     * or reduces player hunger value if there is no saturation
     */
    private void tickAllOnlinePlayersHunger() {
        for (UUID uuid : RunicCoreAPI.getLoadedCharacters()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            if (RunicCoreAPI.isSafezone(player.getLocation())) { // prevent hunger loss in capital cities
                if (player.getFoodLevel() < 20) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> restoreHunger(player));
                }
                continue;
            }
            if (player.getFoodLevel() <= STARVATION_HUNGER_LEVEL) continue;
            if (player.getSaturation() > 0) continue;
            player.setFoodLevel(player.getFoodLevel() - 1);
        }
    }

    private void restoreHunger(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "You feel safe within the city! Your hunger has been restored.");
        player.setFoodLevel(20);
    }

    /**
     * Prevents normal decay of hunger
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (event.getFoodLevel() <= player.getFoodLevel())
            event.setCancelled(true);
    }

    /**
     * Reduces regen for players below half hunger
     */
    @EventHandler
    public void onHealthRegen(HealthRegenEvent event) {
        int foodLevel = event.getPlayer().getFoodLevel();
        if (foodLevel <= INVIGORATED_HUNGER_THRESHOLD) {
            event.setCancelled(true);
        } else if (foodLevel <= 10) {
            event.setAmount((int) (event.getAmount() * HALF_HUNGER_REGEN_MULTIPLIER));
        }
    }

    /**
     * Prevent eating items which are not consumables
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onFoodInteract(PlayerItemConsumeEvent event) {
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(event.getItem());
        if (runicItem == null) return;
        boolean isConsumable = runicItem.getTags().contains(RunicItemTag.CONSUMABLE);
        if (!isConsumable) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "I can't eat that!");
        }
    }
}
