package com.runicrealms.plugin.donor.boost;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.utilities.ClassUtil;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.donor.boost.api.Boost;
import com.runicrealms.plugin.donor.boost.api.BoostAPI;
import com.runicrealms.plugin.donor.boost.api.BoostExperienceType;
import com.runicrealms.plugin.donor.boost.api.StoreBoost;
import com.runicrealms.plugin.donor.boost.event.BoostActivateEvent;
import com.runicrealms.plugin.donor.boost.event.BoostEndEvent;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.runicrestart.RunicRestart;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class BoostManager implements BoostAPI, Listener {

    private static final int RESTART_BUFFER_DURATION_MIN = 10; // What is the minimum number of minutes between the end of a booster, and a server restart
    private static final int BOSS_BAR_CYCLE_DURATION = 20 * 5; // In ticks


    private final Map<UUID, Map<StoreBoost, Integer>> playerBoosts = new ConcurrentHashMap<>();
    private final List<ActiveBoost> activeBoosts = new ArrayList<>();
    private final long startTimestamp = System.currentTimeMillis(); // For cancelling restarts
    private final BossBar activeBossBar = Bukkit.createBossBar("", BarColor.GREEN, BarStyle.SOLID);
    private boolean hasDelayedRestart = false; // Checks if we have already delayed a runic restart. If so, prevent more bombs from being thrown.

    public BoostManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        AtomicReference<ActiveBoost> displayedBoost = new AtomicReference<>(null);
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            if (activeBoosts.size() > 0) {
                if (displayedBoost.get() == null) {
                    displayedBoost.set(activeBoosts.get(activeBoosts.size() - 1));
                } else {
                    int index = activeBoosts.indexOf(displayedBoost.get()) + 1;
                    if (index >= activeBoosts.size()) index = 0;
                    displayedBoost.set(activeBoosts.get(index));
                }
            } else if (displayedBoost.get() != null) {
                displayedBoost.set(null);
            }

            if (activeBoosts.size() > 0 && !activeBossBar.isVisible()) activeBossBar.setVisible(true);
            else if (activeBoosts.size() == 0 && activeBossBar.isVisible()) activeBossBar.setVisible(false);
            if (displayedBoost.get() != null) displayedBoost.get().applyBossBar(activeBossBar);
        }, 0L, BOSS_BAR_CYCLE_DURATION);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RunicCommon.getLuckPermsAPI().retrieveData(event.getPlayer().getUniqueId()).thenAcceptAsync(data -> {
            Map<StoreBoost, Integer> boosts = new HashMap<>();
            for (StoreBoost boost : StoreBoost.values()) {
                if (data.getKeys().contains(boost.getPermission())) {
                    boosts.put(boost, data.getInteger(boost.getPermission()));
                } else {
                    boosts.put(boost, 0);
                }
            }
            playerBoosts.put(event.getPlayer().getUniqueId(), boosts);
        });
    }

    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        activeBossBar.addPlayer(event.getPlayer());
        for (ActiveBoost activeBoost : activeBoosts) {
            Boost boost = activeBoost.getBoost();
            event.getPlayer().sendMessage(ColorUtil.format(
                    "&5[Runic Realms] &dThere is an active &r&f&l" + boost.getName() + " Experience Boost &r&dfor &f"
                            + (1 + boost.getAdditionalMultiplier()) + "x&d experience for &f" + (activeBoost.getRemainingSeconds() / 60) + " &dminutes!"
            ));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerBoosts.remove(event.getPlayer().getUniqueId());
        activeBossBar.removePlayer(event.getPlayer());
    }

    @Override
    public void addStoreBoost(UUID target, StoreBoost boost, int count) {
        playerBoosts.get(target).put(boost, playerBoosts.get(target).get(boost) + count);
        saveCurrentBoosts(target);
    }

    @Override
    public void addStoreBoost(UUID target, StoreBoost boost) {
        addStoreBoost(target, boost, 1);
    }

    @Override
    public void activateStoreBoost(Player player, StoreBoost boost) {
        UUID target = player.getUniqueId();
        if (activeBoosts.stream().anyMatch(activeBoost -> activeBoost.getBoost() == boost))
            throw new IllegalStateException("Cannot activate " + boost.getIdentifier() + ": one is already active");
        playerBoosts.get(target).put(boost, playerBoosts.get(target).get(boost) - 1);
        saveCurrentBoosts(target);
        activateBoost(player, boost);
    }

    @Override
    public void activateBoost(Player activator, Boost boost) {
        ActiveBoost activeBoost = new ActiveBoost(boost, ColorUtil.format("&a&l" + activator.getName() + "'s &r&f&l" + boost.getName() + " &r&a&lExperience Boost"));
        activeBoosts.add(activeBoost);
        activeBoost.applyBossBar(activeBossBar);
        if (!activeBossBar.isVisible()) activeBossBar.setVisible(true);

        Bukkit.getOnlinePlayers().forEach(online -> {
            ClassUtil.launchFirework(online, Color.GREEN);
            online.sendMessage(ColorUtil.format(
                    "&5[Runic Realms] &f" + activator.getName() + " &dhas activated a &f"
                            + (1 + boost.getAdditionalMultiplier()) + "x &dexperience &r&f&l" + boost.getName()
                            + " Boost&r&d for &f" + boost.getDuration() + " &dminutes!"
            ));
        });
        Bukkit.getPluginManager().callEvent(new BoostActivateEvent(activator, boost));

        final String playerName = activator.getName();
        final UUID activatorUUID = activator.getUniqueId();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            if (activeBoosts.size() == 1 && activeBossBar.isVisible()) activeBossBar.setVisible(false);
            activeBoosts.remove(activeBoost);
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
        return activeBoosts.stream().map(ActiveBoost::getBoost).collect(Collectors.toList());
    }

    @Override
    public boolean isBoostActive(Boost boost) {
        return activeBoosts.stream().anyMatch(activeBoost -> activeBoost.getBoost() == boost);
    }

    @Override
    public boolean hasDelayedRestart() {
        return this.hasDelayedRestart;
    }

    @Override
    public double getAdditionalExperienceMultiplier(BoostExperienceType experienceType) {
        double total = 0.0;
        for (ActiveBoost activeBoost : activeBoosts) {
            if (activeBoost.getBoost().getExperienceType() == experienceType)
                total += activeBoost.getBoost().getAdditionalMultiplier();
        }
        return total;
    }

    private void saveCurrentBoosts(UUID target) {
        RunicCommon.getLuckPermsAPI().savePayload(RunicCommon.getLuckPermsAPI().createPayload(target, (data) -> {
            for (StoreBoost storedBoost : playerBoosts.get(target).keySet()) {
                if (!data.containsKey(storedBoost.getPermission()) && playerBoosts.get(target).get(storedBoost) == 0)
                    continue;
                data.set(storedBoost.getPermission(), playerBoosts.get(target).get(storedBoost));
            }
        }));
    }

    private static class ActiveBoost {

        private final long startTimeStamp = System.currentTimeMillis();
        private final String title;
        private final Boost boost;

        private ActiveBoost(Boost boost, String bossBarTitle) {
            this.boost = boost;
            this.title = bossBarTitle;
        }

        public Boost getBoost() {
            return this.boost;
        }

        public int getRemainingSeconds() {
            return (int) (((boost.getDuration() * 60000L) + startTimeStamp - System.currentTimeMillis()) / 1000);
        }

        public int getProgressSeconds() {
            return (int) ((System.currentTimeMillis() - startTimeStamp) / 1000);
        }

        public double getBossBarProgress() {
            return ((double) getRemainingSeconds()) / (boost.getDuration() * 60.0)
        }

        public void applyBossBar(BossBar bossBar) {
            double progress = getBossBarProgress();
            if (progress >= 0) bossBar.setProgress(progress);
            bossBar.setTitle(title);
        }

    }

}
