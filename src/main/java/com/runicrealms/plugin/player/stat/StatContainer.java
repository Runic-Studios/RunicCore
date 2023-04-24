package com.runicrealms.plugin.player.stat;

import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.Stat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * A thread-safe implementation of a container to hold the player's stats, which can be changed by:
 * - Items
 * - Skill Trees
 */
public class StatContainer {
    private final Player player;
    private Map<Stat, Integer> statMap = new HashMap<>();

    public StatContainer(Player player) {
        this.player = player;
    }

    /**
     * Returns the bonus value for the given stat from all armor pieces (including offhand) the player is wearing
     *
     * @param stat to check
     * @return added item bonus for stat
     */
    public int getItemStatBonus(Stat stat) {
        if (RunicItemsAPI.getAddedPlayerStats(player.getUniqueId()).getAddedStats().get(stat) != null)
            return RunicItemsAPI.getAddedPlayerStats(player.getUniqueId()).getAddedStats().get(stat);
        else
            return 0;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the current value of the stat for the player. Includes the cached, changeable values (combat skills)
     * And grabs the cached stat values from armor
     *
     * @param stat to check
     * @return the value for stat
     */
    public int getStat(Stat stat) {
        if (statMap.containsKey(stat))
            return statMap.get(stat) + getItemStatBonus(stat);
        else
            return getItemStatBonus(stat);
    }

    /**
     * Increases the given stat for player. MUST BE CALLED ASYNC
     *
     * @param stat  to change value for
     * @param value the value to decrease the given stat by (parameter value should be positive)
     */
    public void increaseStat(Stat stat, int value) {
        if (!statMap.containsKey(stat))
            statMap.put(stat, value);
        else
            statMap.put(stat, statMap.get(stat) + value);
        // Call custom event for listeners
        StatChangeEvent statChangeEvent = new StatChangeEvent(this.player, this);
        Bukkit.getPluginManager().callEvent(statChangeEvent);
    }

    /**
     * Resets all in-memory stat values for the player to 0
     */
    public void resetValues() {
        this.statMap = new HashMap<>();
        // Call custom event for listeners
        StatChangeEvent statChangeEvent = new StatChangeEvent(this.player, this);
        Bukkit.getPluginManager().callEvent(statChangeEvent);
    }
}
