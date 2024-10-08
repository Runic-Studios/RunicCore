package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.CombatAPI;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.rdb.RunicDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages combat against mobs and players and handles each differently,
 * which is why we need both a HashMap and a List.
 */
public class CombatManager implements CombatAPI, Listener {
    public static final double COMBAT_DURATION_MOBS = 10;
    public static final double COMBAT_DURATION_PLAYERS = 30;
    private final ConcurrentHashMap<UUID, CombatPayload> playersInCombat;

    public CombatManager() {
        this.playersInCombat = new ConcurrentHashMap<>();
        this.startCombatTask();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @Override
    public void enterCombat(UUID uuid, CombatType combatType) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        if (!playersInCombat.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You have entered combat!");
        }
        if (playersInCombat.containsKey(uuid) && playersInCombat.get(uuid).getCombatType() == CombatType.PLAYER)
            combatType = CombatType.PLAYER; // Once you're in PvP, can't switch to mob combat until you drop combat
        playersInCombat.put(uuid, new CombatPayload(player, System.currentTimeMillis(), combatType));
    }

    @Override
    public void giveCombatExp(Player player, int exp) {
        PlayerLevelUtil.giveExperience(player, exp);
    }

    @Override
    public boolean isInCombat(UUID uuid) {
        return this.playersInCombat.containsKey(uuid);
    }

    @Override
    public CombatType getCombatType(UUID uuid) {
        if (!playersInCombat.containsKey(uuid)) return null;
        return playersInCombat.get(uuid).getCombatType();
    }

    @Override
    public void leaveCombat(UUID uuid) {
        playersInCombat.remove(uuid);
    }

    /**
     * starts the repeating task to manage pve/pvp timers
     */
    private void startCombatTask() {
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), () -> {
            for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
                Player player = Bukkit.getPlayer(uuid);

                if (player == null || !playersInCombat.containsKey(uuid)) {
                    continue;
                }

                CombatType combatType = playersInCombat.get(uuid).getCombatType();
                double duration = combatType == CombatType.PLAYER ? COMBAT_DURATION_PLAYERS : COMBAT_DURATION_MOBS;

                if (System.currentTimeMillis() - playersInCombat.get(uuid).getLastRefreshTime() >= (duration * 1000)) {
                    Bukkit.getPluginManager().callEvent(new LeaveCombatEvent(player));
                }
            }
        }, 0, 20);
    }

    static class CombatPayload {
        private final Player player;
        private long lastRefreshTime;
        private CombatType combatType;

        public CombatPayload(Player player, long lastRefreshTime, CombatType combatType) {
            this.player = player;
            this.lastRefreshTime = lastRefreshTime;
            this.combatType = combatType;
        }

        public Player getPlayer() {
            return player;
        }

        public long getLastRefreshTime() {
            return lastRefreshTime;
        }

        public void setLastRefreshTime(long lastRefreshTime) {
            this.lastRefreshTime = lastRefreshTime;
        }

        public CombatType getCombatType() {
            return combatType;
        }

        public void setCombatType(CombatType combatType) {
            this.combatType = combatType;
        }
    }

}
