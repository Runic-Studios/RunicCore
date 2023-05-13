package com.runicrealms.plugin.player.death;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicrestart.event.PreShutdownEvent;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GravestoneManager implements Listener {
    private final Map<UUID, Gravestone> gravestoneMap = new HashMap<>();

    public GravestoneManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        startGravestoneTask();
    }

    public Map<UUID, Gravestone> getGravestoneMap() {
        return gravestoneMap;
    }

    /**
     * Prevents destruction of soil, interaction with tons of new blocks
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getType().toString().toLowerCase().contains("shulker")) {
                event.setCancelled(true);
                gravestoneMap.forEach((uuid, gravestone) -> {
                    if (gravestone.getShulkerBox().getBlock().equals(event.getClickedBlock())) {
                        if (!gravestone.isPriority() || event.getPlayer().getUniqueId().equals(gravestone.getUuid())) {
                            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.5f, 1.0f);
                            event.getPlayer().openInventory(gravestone.getInventory());
                        } else {
                            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                            event.getPlayer().sendMessage(ChatColor.RED + "Only slain player can loot this chest until priority ends!");
                        }
                    }
                });
            }
        }
    }


    @EventHandler
    public void onShutdown(PreShutdownEvent event) {
        for (UUID uuid : gravestoneMap.keySet()) {
            Gravestone gravestone = gravestoneMap.get(uuid);
            // todo: drop items
            gravestone.getShulkerBox().getLocation().getBlock().setType(Material.AIR);
            gravestoneMap.remove(uuid);
        }
    }

    private void startGravestoneTask() {
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            for (UUID uuid : gravestoneMap.keySet()) {
                Gravestone gravestone = gravestoneMap.get(uuid);
                Hologram hologram = gravestone.getHologram();

                long currentTime = System.currentTimeMillis();
                long elapsedTimeInSeconds = (currentTime - gravestone.getStartTime()) / 1000; // Convert from milliseconds to seconds

                int remainingPriorityTime = Gravestone.PRIORITY_TIME - (int) elapsedTimeInSeconds;
                int duration = Gravestone.DURATION - (int) elapsedTimeInSeconds;

                // Calculate minutes and seconds
                int minutesPriority = remainingPriorityTime / 60;
                int secondsPriority = remainingPriorityTime % 60;
                int minutesDuration = duration / 60;
                int secondsDuration = duration % 60;

                // Format the remaining time
                String priorityFormatted = String.format("%dm%ds", minutesPriority, secondsPriority);
                String durationFormatted = String.format("%dm%ds", minutesDuration, secondsDuration);

                // Update the priority timer line of the hologram
                hologram.removeLine(1); // Remove the old line
                hologram.insertTextLine(1, ChatColor.YELLOW + "Priority: " + priorityFormatted); // Add the updated line

                // Update the duration timer line of the hologram
                hologram.removeLine(2); // Remove the old line
                hologram.insertTextLine(2, ChatColor.GRAY + "Time left: " + durationFormatted);
            }
        }, 0, 20L);
    }


}
