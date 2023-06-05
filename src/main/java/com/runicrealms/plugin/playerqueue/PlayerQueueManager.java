package com.runicrealms.plugin.playerqueue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
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
import java.util.Optional;
import java.util.UUID;

public class PlayerQueueManager implements Listener {

    private static final String[] PRIORITY_QUEUE_PERMISSIONS = {
            "runic.rank.hero",
            "runic.rank.champion",
            "runic.rank.knight",
            "runic.rank.alpha"
    };
    private static final String BYPASS_PERMISSION = "runic.team";
    private final int MAX_PLAYER_SLOTS = RunicCore.getInstance().getConfig().getInt("server-slots");
    private final int QUEUE_SAVE_MIN = 2; // how many minutes do we save your spot for
    private final long MAX_JOIN_INTERVAL = 5 * 60 * 1000; // explanation:
    // If you joined 4 minutes ago and someone just logged off, we will save your spot for another 3 minutes
    // If you joined 6 minutes ago and someone just logged off, you have been inactive for too long and will not have a spot saved (removed from queue)

    private final LinkedList<QueuedPlayer> joinQueue = new LinkedList<>(); // boolean is priority queue or no
    private final Map<UUID, BukkitTask> savedSpots = new HashMap<>();

    public PlayerQueueManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getPlayer().hasPermission(BYPASS_PERMISSION)) {
            event.allow();
        } else {
//        System.out.println("Join queue pre: " + joinQueue.stream().map((player) -> player.uuid().toString()).collect(Collectors.joining(", ")));
//        System.out.println("saved spots pre: " + savedSpots.keySet().stream().map(UUID::toString).collect(Collectors.joining(", ")));
            if (!savedSpots.containsKey(event.getPlayer().getUniqueId())) {
                if (Bukkit.getOnlinePlayers().size() + savedSpots.size() >= MAX_PLAYER_SLOTS) {
                    boolean priority = canJoinPriorityQueue(event.getPlayer());
                    // Handle adding player to the join queue
                    Optional<QueuedPlayer> optPlayer = joinQueue.stream().filter((player) -> player.getUUID().equals(event.getPlayer().getUniqueId())).findFirst();
                    if (optPlayer.isEmpty()) {
                        addToQueue(event.getPlayer().getUniqueId(), priority);
                    } else {
                        // Update last time they joined
                        optPlayer.get().updateLastJoin();
                    }
                    // Handle message
                    StringBuilder message = new StringBuilder("&cThe server is full (" + MAX_PLAYER_SLOTS + "). ");
                    if (priority) message.append("Your PRIORITY queue position: ");
                    else message.append("Your queue position: ");
                    message.append(findUUIDIndex(event.getPlayer().getUniqueId()) + 1);
                    message.append("\n&cOnce it's your turn, your position will be saved for " + QUEUE_SAVE_MIN + " minutes");
                    if (!priority) message.append("\n&c&lPurchase a rank to gain access to the priority queue!");
                    event.disallow(PlayerLoginEvent.Result.KICK_FULL, ColorUtil.format(message.toString()));
                } else {
                    // Player was on the join queue and is joining now that the queue is empty (shouldn't happen, but safety net)
                    if (joinQueue.stream().anyMatch((player) -> player.getUUID().equals(event.getPlayer().getUniqueId()))) {
                        joinQueue.removeIf((player) -> player.getUUID().equals(event.getPlayer().getUniqueId()));
                    }
                }
            } else {
                event.allow();
                savedSpots.get(event.getPlayer().getUniqueId()).cancel();
                savedSpots.remove(event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        onPlayerQuitHandle();
    }

    private void onPlayerQuitHandle() {
        if (Bukkit.getOnlinePlayers().size() + savedSpots.size() == MAX_PLAYER_SLOTS) {
            if (!joinQueue.isEmpty()) {
                UUID uuid = null;
                while (uuid == null) {
                    if (joinQueue.isEmpty()) return;
                    QueuedPlayer target = joinQueue.peekFirst();
                    if (target.getLastJoin() + MAX_JOIN_INTERVAL <= System.currentTimeMillis()) {
                        joinQueue.removeFirst();
                    } else {
                        uuid = target.getUUID();
                    }
                }
                UUID finalUuid = uuid;
                savedSpots.put(finalUuid, Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
                    savedSpots.remove(finalUuid);
                    onPlayerQuitHandle();
                }, 20 * 60 * QUEUE_SAVE_MIN));
            }
        }
    }

    private void addToQueue(UUID targetUuid, boolean priority) {
        if (priority) {
            QueuedPlayer playerToAdd = new QueuedPlayer(targetUuid, true, System.currentTimeMillis());
            if (joinQueue.isEmpty()) {
                joinQueue.add(playerToAdd);
            } else {
                ListIterator<QueuedPlayer> iterator = joinQueue.listIterator();
                boolean added = false;
                while (iterator.hasNext()) {
                    QueuedPlayer entry = iterator.next();
                    if (!entry.hasPriority()) {
                        iterator.previous();
                        iterator.add(new QueuedPlayer(targetUuid, true, System.currentTimeMillis()));
                        added = true;
                        break;
                    }
                }
                if (!added) iterator.add(playerToAdd);
            }
        } else {
            joinQueue.add(new QueuedPlayer(targetUuid, false, System.currentTimeMillis()));
        }
    }

    private int findUUIDIndex(UUID targetUuid) {
        ListIterator<QueuedPlayer> it = joinQueue.listIterator();
        while (it.hasNext()) {
            QueuedPlayer player = it.next();
            if (player.getUUID().equals(targetUuid)) {
                return it.previousIndex();
            }
        }
        return -1;
    }

    private boolean canJoinPriorityQueue(Player player) {
        for (String perm : PRIORITY_QUEUE_PERMISSIONS) {
            if (player.hasPermission(perm)) return true;
        }
        return false;
    }

}
