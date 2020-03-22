package com.runicrealms.plugin.player.combat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ActionBarUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * This class manages combat against mobs and players and handles each differently,
 * which is why we need both a HashMap and a List.
 */
public class CombatManager implements Listener {

    private HashMap<UUID, Long> playersInCombat;
    private List<UUID> pvpers = new ArrayList<>();
    private RunicCore plugin = RunicCore.getInstance();
    private static final double COMBAT_DURATION = 10;

    public CombatManager() {
        this.playersInCombat = new HashMap<>();
        this.startCombatTask();
    }

    public double getCombatDuration() { return COMBAT_DURATION; }
    public HashMap<UUID, Long> getPlayersInCombat() {
        return this.playersInCombat;
    }
    public List<UUID> getPvPers() {
        return this.pvpers;
    }

    public void addPlayer(UUID uuid) {
        playersInCombat.put(uuid, System.currentTimeMillis());
    }

    // starts the repeating task to manage pve/pvp timers
    private void startCombatTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player online : RunicCore.getCacheManager().getLoadedPlayers()) {
                    if(playersInCombat.containsKey(online.getUniqueId())) {
                        if (System.currentTimeMillis() - playersInCombat.get(online.getUniqueId()) >= (COMBAT_DURATION*1000)) {
                            playersInCombat.remove(online.getUniqueId());
                            pvpers.remove(online.getUniqueId());
                            ActionBarUtil.sendTimedMessage(online, "&aYou have left combat!", 3);
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 20);
    }
}
