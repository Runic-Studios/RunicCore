package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class that exists to make sure that {@link org.bukkit.event.player.PlayerInteractEvent} is still fired even if the player is nearby within 3-5 blocks of a mob
 * Reference <a href="https://www.spigotmc.org/threads/1-19-playerinteractevent-not-called-when-entity-is-in-sight-client-bug.574671/">...</a>
 *
 * @author BoBoBalloon
 */
public class PlayerInteractCorrectionListener implements Listener {
    private final Map<UUID, Long> playerInteractions;

    public PlayerInteractCorrectionListener() {
        this.playerInteractions = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerAnimationEvent(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }

        long current = System.currentTimeMillis();

        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            Long time = this.playerInteractions.remove(event.getPlayer().getUniqueId());

            if (time != null && time + 10 > current) { //less than 10 milliseconds
                return;
            }

            ItemStack item = event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR ? event.getPlayer().getInventory().getItemInMainHand() : null;

            PlayerInteractEvent interactEvent = new PlayerInteractEvent(event.getPlayer(), Action.LEFT_CLICK_AIR, item, null, event.getPlayer().getFacing());

            Bukkit.getPluginManager().callEvent(interactEvent);
        }, 2); //one tick delay is too early for the damage listener
    }

    //Listen for all other causes of arm animation and check if they were fired

    @EventHandler(priority = EventPriority.HIGHEST) //event is cancelled by other runic stuff, we ignore it
    private void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        this.playerInteractions.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerDropItem(PlayerDropItemEvent event) {
        this.playerInteractions.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        this.playerInteractions.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        this.playerInteractions.remove(event.getPlayer().getUniqueId());
    }
}
