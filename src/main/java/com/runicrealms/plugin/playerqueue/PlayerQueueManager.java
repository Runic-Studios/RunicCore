package com.runicrealms.plugin.playerqueue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

public class PlayerQueueManager implements Listener {

    private static final String[] priorityQueuePermissions = {
            "runic.rank.hero",
            "runic.rank.champion",
            "runic.rank.knight",
            "runic.rank.alpha"
    };
    private final int MAX_PLAYER_SLOTS = RunicCore.getInstance().getConfig().getInt("server-slots");
    private final LinkedList<Pair<UUID, Boolean>> joinQueue = new LinkedList<>(); // boolean is priority queue or no
    private final Map<UUID, BukkitTask> savedSpots = new HashMap<>();

    public PlayerQueueManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!savedSpots.containsKey(event.getPlayer().getUniqueId())) {
            if (Bukkit.getOnlinePlayers().size() + savedSpots.size() >= MAX_PLAYER_SLOTS) {
                // Handle adding player to the join queue
                if (joinQueue.stream().noneMatch((pair) -> pair.first.equals(event.getPlayer().getUniqueId()))) {
                    if (canJoinPriorityQueue(event.getPlayer())) {
                        addToPriorityQueue(event.getPlayer().getUniqueId());
                    } else {
                        joinQueue.add(new Pair<>(event.getPlayer().getUniqueId(), false));
                    }
                }
                // Handle message
                if (canJoinPriorityQueue(event.getPlayer())) {
                    event.disallow(PlayerLoginEvent.Result.KICK_FULL, ColorUtil.format("&cThe server is full (" + MAX_PLAYER_SLOTS + "). "
                            + ". Your PRIORITY queue position: " + findUUIDIndex(event.getPlayer().getUniqueId())
                            + "\n&cOnce it's your turn, your position will be saved for 3 minutes"));
                } else {
                    event.disallow(PlayerLoginEvent.Result.KICK_FULL, ColorUtil.format("&cThe server is full (" + MAX_PLAYER_SLOTS + "). "
                            + "Your queue position: " + findUUIDIndex(event.getPlayer().getUniqueId())
                            + "\n&cOnce it's your turn, your position will be saved for 3 minutes"
                            + "\n&c&lPurchase a rank to gain access to the priority queue!"));
                }
            } else {
                // Player was on the join queue and is joining now that the queue is empty (shouldn't happen, but safety net)
                if (joinQueue.stream().anyMatch((pair) -> pair.first.equals(event.getPlayer().getUniqueId()))) {
                    joinQueue.removeIf((pair) -> pair.first.equals(event.getPlayer().getUniqueId()));
                }
            }
        } else {
            event.allow();
            savedSpots.get(event.getPlayer().getUniqueId()).cancel();
            savedSpots.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        onPlayerQuitHandle();
    }

    private void onPlayerQuitHandle() {
        if (Bukkit.getOnlinePlayers().size() + savedSpots.size() == MAX_PLAYER_SLOTS) {
            if (!joinQueue.isEmpty()) {
                UUID uuid = joinQueue.removeFirst().first;
                savedSpots.put(uuid, Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
                    savedSpots.remove(uuid);
                    onPlayerQuitHandle();
                }, 20 * 60 * 3));
            }
        }
    }

    private void addToPriorityQueue(UUID targetUuid) {
        if (joinQueue.isEmpty()) {
            joinQueue.add(new Pair<>(targetUuid, true));
        } else {
            ListIterator<Pair<UUID, Boolean>> iterator = joinQueue.listIterator();
            boolean added = false;
            while (iterator.hasNext()) {
                Pair<UUID, Boolean> entry = iterator.next();
                if (!entry.second) {
                    iterator.previous();
                    iterator.add(new Pair<>(targetUuid, true));
                    added = true;
                    break;
                }
            }
            if (!added) iterator.add(new Pair<>(targetUuid, true));
        }
    }

    private int findUUIDIndex(UUID targetUuid) {
        ListIterator<Pair<UUID, Boolean>> it = joinQueue.listIterator();
        while (it.hasNext()) {
            Pair<UUID, Boolean> pair = it.next();
            if (pair.first.equals(targetUuid)) {
                return it.previousIndex();
            }
        }
        return -1;
    }

    private boolean canJoinPriorityQueue(Player player) {
        for (String perm : priorityQueuePermissions) {
            if (player.hasPermission(perm)) return true;
        }
        return false;
    }

}
