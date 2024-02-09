package com.runicrealms.plugin.player.death;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedEnumEntityUseAction;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.npcs.RunicNpcs;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicrestart.event.PreShutdownEvent;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GravestoneManager implements Listener {
    private final ConcurrentHashMap<UUID, Gravestone> gravestoneMap = new ConcurrentHashMap<>();

    public GravestoneManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        // Listen async for USE_ENTITY packet (FallingBlock)
        registerPacketListener();
        startGravestoneTask();
    }

    private void registerPacketListener() {
        ProtocolLibrary.getProtocolManager().getAsynchronousManager().registerAsyncHandler(new PacketAdapter(RunicNpcs.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
                    PacketContainer packet = event.getPacket();
                    WrappedEnumEntityUseAction useAction = packet.getEnumEntityUseActions().readSafely(0);
                    EnumWrappers.EntityUseAction action = useAction.getAction();

                    if (action == EnumWrappers.EntityUseAction.ATTACK) return;
                    if (action == EnumWrappers.EntityUseAction.INTERACT) return;
                    if (action == EnumWrappers.EntityUseAction.INTERACT_AT) {
                        if (useAction.getHand() == EnumWrappers.Hand.OFF_HAND) return;
                        int entityID = packet.getIntegers().read(0);
                        if (gravestoneMap.isEmpty()) return;
                        Optional<Gravestone> gravestone = gravestoneMap.values().stream().filter(stone -> stone.getFallingBlock().getEntityId() == entityID).findFirst();
                        gravestone.ifPresent(value -> attemptToOpenGravestone(event.getPlayer(), value));
                    }
                }
            }
        }).start();
    }

    private void attemptToOpenGravestone(Player player, Gravestone gravestone) {
        if (canOpenGravestone(gravestone.getUuid(), player, gravestone)) {
            // Open inventory sync
            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 0.5f, 1.0f);
                player.openInventory(gravestone.getInventory());
            });
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Only slain player and their party can loot this chest until priority ends!");
        }
    }

    private boolean canOpenGravestone(UUID uuid, Player whoOpened, Gravestone gravestone) {
        if (gravestoneMap.get(whoOpened.getUniqueId()) != null && gravestoneMap.get(whoOpened.getUniqueId()).equals(gravestone))
            return true; // Always true if it is the slain player
        if (!gravestone.hasPriority())
            return true; // True if priority has expired
        // Gravestone has priority
        return RunicCore.getPartyAPI().isPartyMember(uuid, whoOpened); // Party members can open gravestone
    }

    public Map<UUID, Gravestone> getGravestoneMap() {
        return gravestoneMap;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (gravestoneMap.isEmpty()) return;
        gravestoneMap.forEach((uuid, gravestone) -> {
            if (gravestone.getInventory().equals(event.getInventory())) {
                // If the action was a placement action (i.e., the clicked inventory is the top one), cancel the event.
                if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
                    return;
                RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(event.getCurrentItem());
                if (RunicItemsAPI.containsBlockedTag(runicItem))
                    event.setCancelled(true);
            }
        });
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (gravestoneMap.isEmpty()) return;
        gravestoneMap.forEach((uuid, gravestone) -> {
            if (gravestone.getInventory().equals(event.getInventory())
                    && event.getInventory().isEmpty()) {
                gravestoneMap.remove(uuid);
                gravestone.collapse(false);
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRunicDeath(RunicDeathEvent event) {
        if (event.isCancelled()) return;
        if (gravestoneMap.isEmpty()) return;
        if (!gravestoneMap.containsKey(event.getVictim().getUniqueId())) return;
        Gravestone gravestone = gravestoneMap.get(event.getVictim().getUniqueId());
        gravestoneMap.remove(event.getVictim().getUniqueId());
        gravestone.collapse(true);
    }

    @EventHandler
    public void onShutdown(PreShutdownEvent event) {
        for (UUID uuid : gravestoneMap.keySet()) {
            Gravestone gravestone = gravestoneMap.get(uuid);
            gravestoneMap.remove(uuid);
            gravestone.collapse(false);
        }
    }

    private void startGravestoneTask() {
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            for (UUID uuid : gravestoneMap.keySet()) {
                Gravestone gravestone = gravestoneMap.get(uuid);

                gravestone.getFallingBlock().setTicksLived(1); // Prevents gravestones from de-spawning

                Hologram hologram = gravestone.getHologram();

                long currentTime = System.currentTimeMillis();
                long elapsedTimeInSeconds = (currentTime - gravestone.getStartTime()) / 1000; // Convert from milliseconds to seconds

                int remainingPriorityTime = gravestone.getPriorityTime() - (int) elapsedTimeInSeconds;
                int duration = gravestone.getDuration() - (int) elapsedTimeInSeconds;

                // Remove gravestone if time is up
                if (duration <= 0) {
                    gravestoneMap.remove(uuid);
                    gravestone.collapse(false);
                    continue;
                } else if (remainingPriorityTime <= 0) {
                    gravestone.setPriority(false);
                }

                // Update hologram if there is no priority
                if (!gravestone.hasPriority()) {
                    remainingPriorityTime = 0;
                }

                // Calculate minutes and seconds
                int minutesPriority = remainingPriorityTime / 60;
                int secondsPriority = remainingPriorityTime % 60;
                int minutesDuration = duration / 60;
                int secondsDuration = duration % 60;

                // Format the remaining time
                String priorityFormatted = String.format("%dm%ds", minutesPriority, secondsPriority);
                String durationFormatted = String.format("%dm%ds", minutesDuration, secondsDuration);

                // Update the priority timer line of the hologram
                hologram.getLines().remove(1); // Remove the old line
                hologram.getLines().insertText(1, ChatColor.YELLOW + "Priority: " + priorityFormatted); // Add the updated line

                // Update the duration timer line of the hologram
                hologram.getLines().remove(2); // Remove the old line
                hologram.getLines().insertText(2, ChatColor.GRAY + "Time left: " + durationFormatted);
            }
        }, 0, 20L);
    }
}
