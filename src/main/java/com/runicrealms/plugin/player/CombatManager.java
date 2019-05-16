package com.runicrealms.plugin.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

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
    private static final int COMBAT_DURATION = 10;

    public CombatManager() {
        this.playersInCombat = new HashMap<>();
        this.startCombatTask();
    }

    public int getCombatDuration() { return COMBAT_DURATION; }
    public HashMap<UUID, Long> getPlayersInCombat() {
        return this.playersInCombat;
    }
    public List<UUID> getPvPers() {
        return this.pvpers;
    }

    public void addPlayer(UUID uuid, Long currentTime) {
        playersInCombat.put(uuid, currentTime);
    }

    // starts the repeating task to manage pvp timers
    private void startCombatTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player online : Bukkit.getOnlinePlayers()) {
                    if(playersInCombat.containsKey(online.getUniqueId())) {
                        if (System.currentTimeMillis() - playersInCombat.get(online.getUniqueId()) >= (COMBAT_DURATION*1000)) {
                            playersInCombat.remove(online.getUniqueId());
                            pvpers.remove(online.getUniqueId());
                            online.sendMessage(ChatColor.GREEN + "You have left combat!");
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 20);
    }
}
