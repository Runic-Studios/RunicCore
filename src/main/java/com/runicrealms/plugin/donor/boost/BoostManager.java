package com.runicrealms.plugin.donor.boost;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.utilities.ClassUtil;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.donor.boost.api.Boost;
import com.runicrealms.plugin.donor.boost.api.BoostAPI;
import com.runicrealms.plugin.donor.boost.api.BoostExperienceType;
import com.runicrealms.plugin.donor.boost.api.StoreBoost;
import com.runicrealms.plugin.donor.boost.event.BoostActivateEvent;
import com.runicrealms.plugin.donor.boost.event.BoostEndEvent;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.runicrestart.RunicRestart;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class BoostManager implements BoostAPI, Listener {

    // What is the minimum number of minutes between the end of a booster, and a server restart
    private static final int RESTART_BUFFER_DURATION_MIN = 10;
    private final Map<UUID, Map<StoreBoost, Integer>> playerBoosts = new ConcurrentHashMap<>();
    private final Map<Boost, BoostBossBar> activeBoosts = new HashMap<>();
    private final long startTimestamp = System.currentTimeMillis(); // For cancelling restarts
    // Checks if we have already delayed a runic restart. If so, prevent more bombs from being thrown.
    private boolean hasDelayedRestart = false;

    public BoostManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            for (Map.Entry<Boost, BoostBossBar> entry : activeBoosts.entrySet()) {
                entry.getValue().updateBossBarProgress();
            }
        }, 0L, 20 * 10);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        DAO.loadBoostCounts(event.getPlayer().getUniqueId()).thenAccept((payload) ->
                playerBoosts.put(event.getPlayer().getUniqueId(), payload.boosts));
    }

    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        for (Boost boost : activeBoosts.keySet()) {
            BoostBossBar boostBossBar = activeBoosts.get(boost);
            boostBossBar.bossBar.addPlayer(event.getPlayer());
            event.getPlayer().sendMessage(ColorUtil.format(
                    "&5[Runic Realms] &dThere is an active &r&f&l" + boost.getName() + " Experience Boost &r&dfor &f"
                            + ((int) (boost.getAdditionalMultiplier() * 100)) + "%&d additional experience for &f" + (boostBossBar.getRemainingSeconds() / 60) + " &dremaining minutes!"
            ));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerBoosts.remove(event.getPlayer().getUniqueId());
        for (BoostBossBar boostBossBar : activeBoosts.values()) {
            boostBossBar.bossBar.removePlayer(event.getPlayer());
        }
    }

    @Override
    public void addStoreBoost(UUID target, StoreBoost boost, int count) {
        playerBoosts.get(target).put(boost, playerBoosts.get(target).get(boost) + count);
        DAO.queueSave(target, playerBoosts.get(target));
    }

    @Override
    public void addStoreBoost(UUID target, StoreBoost boost) {
        addStoreBoost(target, boost, 1);
    }

    @Override
    public void activateStoreBoost(Player player, StoreBoost boost) {
        UUID target = player.getUniqueId();
        if (activeBoosts.containsKey(boost))
            throw new IllegalStateException("Cannot activate " + boost.getIdentifier() + ": one is already active");
        playerBoosts.get(target).put(boost, playerBoosts.get(target).get(boost) - 1);
        DAO.queueSave(target, playerBoosts.get(target));
        activateBoost(player, boost);
    }

    @Override
    public void activateBoost(Player activator, Boost boost) {
        BoostBossBar boostBossBar = new BoostBossBar(Bukkit.createBossBar(
                ColorUtil.format("&4&l" + activator.getName() + "&r&c&l's " + boost.getName() + " Experience Boost"),
                BarColor.RED,
                BarStyle.SOLID
        ), boost);
        activeBoosts.put(boost, boostBossBar);

        Bukkit.getOnlinePlayers().forEach(online -> {
            activeBoosts.get(boost).bossBar.addPlayer(online);
            ClassUtil.launchFirework(online, Color.RED);
            online.sendMessage(ColorUtil.format(
                    "&5[Runic Realms] &f" + activator.getName() + " &dhas activated a &f"
                            + ((int) (boost.getAdditionalMultiplier() * 100)) + "% &dadditional experience &r&f&l" + boost.getName()
                            + " Boost&r&d for &f" + boost.getDuration() + " &dminutes!"
            ));
        });
        Bukkit.getPluginManager().callEvent(new BoostActivateEvent(activator, boost));

        final String playerName = activator.getName();
        final UUID activatorUUID = activator.getUniqueId();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            activeBoosts.get(boost).bossBar.removeAll();
            activeBoosts.remove(boost);
            Bukkit.broadcastMessage(ColorUtil.format("&5[Runic Realms] &f" + playerName + "&d's &f&l" + boost.getName()
                    + " Experience Boost &r&dhas ended! Visit &fstore.runicrealms.com&d to purchase more."));
            Bukkit.getPluginManager().callEvent(new BoostEndEvent(playerName, activatorUUID, boost));
        }, boost.getDuration() * 60 * 20L);


        long currentRestartTimestamp = startTimestamp + RunicRestart.getRestartManager().getDefaultLifetime() * 60000L;
        long projectedMinimumRestartTimestamp = System.currentTimeMillis() + (boost.getDuration() + RESTART_BUFFER_DURATION_MIN) * 60000L;
        if (currentRestartTimestamp <= projectedMinimumRestartTimestamp) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "runicrestart delay " + (boost.getDuration() + RESTART_BUFFER_DURATION_MIN) + " quiet");
            Bukkit.broadcastMessage(ColorUtil.format("&4[Notice] &cServer restart has been delayed due to active booster!"));
            hasDelayedRestart = true;
        }
    }

    @Override
    public int getStoreBoostCount(UUID target, StoreBoost boost) {
        return playerBoosts.get(target).get(boost);
    }

    @Override
    public boolean hasStoreBoost(UUID target, StoreBoost boost) {
        return playerBoosts.get(target).get(boost) > 0;
    }

    @Override
    public Collection<Boost> getCurrentActiveBoosts() {
        return activeBoosts.keySet();
    }

    @Override
    public boolean isBoostActive(Boost boost) {
        return activeBoosts.containsKey(boost);
    }

    @Override
    public boolean hasDelayedRestart() {
        return this.hasDelayedRestart;
    }

    @Override
    public double getAdditionalExperienceMultiplier(BoostExperienceType experienceType) {
        return activeBoosts.keySet().stream()
                .filter((boost) -> boost.getExperienceType() == experienceType)
                .mapToDouble(Boost::getAdditionalMultiplier)
                .sum();
    }

    private static class BoostBossBar {

        private final BossBar bossBar;
        private final long startTimeStamp = System.currentTimeMillis();
        private final int boostDuration; // Minutes

        private BoostBossBar(BossBar bossBar, Boost boost) {
            this.bossBar = bossBar;
            this.boostDuration = boost.getDuration();
        }

        public int getRemainingSeconds() {
            return (int) (((boostDuration * 60000L) + startTimeStamp - System.currentTimeMillis()) / 1000);
        }

        public int getProgressSeconds() {
            return (int) ((System.currentTimeMillis() - startTimeStamp) / 1000);
        }

        public void updateBossBarProgress() {
            bossBar.setProgress(((double) getRemainingSeconds()) / (boostDuration * 60.0));
        }

    }

    // Data access object
    private static class DAO {
        // This prevents us from saving boosts more than one at a time
        private static final ConcurrentLinkedQueue<Payload> queuedSaves = new ConcurrentLinkedQueue<>();
        private static CompletableFuture<Void> currentFuture = CompletableFuture.completedFuture(null);

        private static CompletableFuture<Payload> loadBoostCounts(UUID target) {
            CompletableFuture<Payload> future = new CompletableFuture<>();
            LuckPermsProvider.get().getUserManager().loadUser(target).thenAcceptAsync(user -> {
                Map<StoreBoost, Integer> boosts = new HashMap<>();
                CachedMetaData meta = user.getCachedData().getMetaData();
                for (StoreBoost boost : StoreBoost.values()) {
                    if (meta.getMeta().containsKey(boost.getPermission())) {
                        boosts.put(boost, Integer.parseInt(Objects.requireNonNull(meta.getMetaValue(boost.getPermission()))));
                    } else {
                        boosts.put(boost, 0);
                    }
                }
                future.complete(new Payload(target, boosts));
            });
            return future;
        }

        private static CompletableFuture<Void> savePayload(Payload payload) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            LuckPermsProvider.get().getUserManager().loadUser(payload.target).thenAcceptAsync(user -> {
                try {
                    CachedMetaData meta = user.getCachedData().getMetaData();
                    for (StoreBoost boost : payload.boosts.keySet()) {
                        if (meta.getMeta().containsKey(boost.getPermission())) {
                            user.data().clear(NodeType.META.predicate(metaNode -> metaNode.getMetaKey().equalsIgnoreCase(boost.getPermission())));
                        }
                        if (payload.boosts.get(boost) > 0)
                            user.data().add(MetaNode.builder(boost.getPermission(), payload.boosts.get(boost).toString()).build());
                    }
                    LuckPermsProvider.get().getUserManager().saveUser(user).thenAcceptAsync(unused -> {
                        future.complete(null);
                    });
                } catch (Exception exception) {
                    Bukkit.getLogger().log(Level.INFO, "ERROR: could not save boost payload!");
                    exception.printStackTrace();
                }
            });
            return future;
        }

        private static void queueSave(UUID target, Map<StoreBoost, Integer> boosts) {
            synchronized (DAO.class) {
                queuedSaves.add(new Payload(target, boosts));
                if (currentFuture.isDone()) {
                    currentFuture = CompletableFuture.runAsync(() -> {
                        Payload payload = queuedSaves.poll();
                        if (payload != null) currentFuture = savePayload(payload);
                    });
                } else {
                    currentFuture = currentFuture.thenRunAsync(() -> {
                        Payload payload = queuedSaves.poll();
                        if (payload != null) currentFuture = savePayload(payload);
                    });
                }
            }
        }

        private record Payload(UUID target, Map<StoreBoost, Integer> boosts) {
        }
    }
}
