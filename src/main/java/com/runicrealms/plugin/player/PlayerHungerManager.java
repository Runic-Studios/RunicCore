package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerHungerManager implements Listener {

    private static final int HUNGER_DAMAGE_TASK_TIME = 4;
    private static final int HUNGER_TICK_TASK_DELAY = 45; // s
    private static final int INVIGORATED_DURATION_SECONDS = 300;
    private static final int PLAYER_HUNGER_TIME = 30; // tick time in seconds
    private static final int STARVATION_HUNGER_LEVEL = 1;
    private static final double SATIATED_DAMAGE_BONUS_PERCENT = 0.05;
    private static final double HUNGER_SELF_DAMAGE_PERCENT = 0.05; // 5% damage or 1/2 heart
    private final Set<UUID> invigoratedPlayers = new HashSet<>(); // regen
    private final Set<UUID> satiatedPlayers = new HashSet<>(); // damage bonus

    public PlayerHungerManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(RunicCore.getInstance(),
                this::tickAllOnlinePlayersHunger, HUNGER_TICK_TASK_DELAY * 20L, PLAYER_HUNGER_TIME * 20L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(RunicCore.getInstance(),
                this::damagePlayersWithoutHunger, HUNGER_TICK_TASK_DELAY * 20L, HUNGER_DAMAGE_TASK_TIME * 20L);
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
            if (player.getFoodLevel() < 17)
                satiatedPlayers.remove(player.getUniqueId());
        }
    }

    /**
     * Damages players with bottom hunger
     */
    private void damagePlayersWithoutHunger() {
        for (Player player : RunicCore.getCacheManager().getLoadedPlayers()) {
            if (player.getGameMode() == GameMode.CREATIVE) continue;
            if (player.getFoodLevel() > STARVATION_HUNGER_LEVEL) continue;
            int maxHealth = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            GenericDamageEvent genericDamageEvent = new GenericDamageEvent
                    (
                            player,
                            (int) (maxHealth * HUNGER_SELF_DAMAGE_PERCENT),
                            GenericDamageEvent.DamageCauses.STARVATION
                    );
            Bukkit.getPluginManager().callEvent(genericDamageEvent);
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
            if (event.getFoodLevel() >= 17) {
                satiatedPlayers.add(player.getUniqueId());
                player.sendMessage
                        (
                                ChatColor.YELLOW + "You feel full! You are now " +
                                        ChatColor.YELLOW + ChatColor.ITALIC + "satiated" +
                                        ChatColor.YELLOW + ", dealing bonus damage!"
                        );
            }
            if (event.getFoodLevel() > player.getFoodLevel()
                    && !invigoratedPlayers.contains(player.getUniqueId())) {
                invigoratedPlayers.add(player.getUniqueId());
                player.sendMessage
                        (
                                ChatColor.YELLOW + "You are now " +
                                        ChatColor.YELLOW + ChatColor.ITALIC +
                                        "invigorated" + ChatColor.YELLOW +
                                        ", regenerating health over time."
                        );
                Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> {
                    invigoratedPlayers.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.GRAY + "You are no longer invigorated.");
                }, INVIGORATED_DURATION_SECONDS * 20L);
            }
        }
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        if (!satiatedPlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() + (e.getAmount() * SATIATED_DAMAGE_BONUS_PERCENT)));
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        if (!satiatedPlayers.contains(e.getPlayer().getUniqueId())) return;
        e.setAmount((int) (e.getAmount() + (e.getAmount() * SATIATED_DAMAGE_BONUS_PERCENT)));
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

    public Set<UUID> getInvigoratedPlayers() {
        return invigoratedPlayers;
    }
}
