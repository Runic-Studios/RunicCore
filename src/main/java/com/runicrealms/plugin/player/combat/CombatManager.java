package com.runicrealms.plugin.player.combat;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                            online.sendMessage(ChatColor.GREEN + "You have left combat!");
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 20);
    }

    public HashMap<UUID, Long> getPlayersInCombat() {
        return this.playersInCombat;
    }
    public List<UUID> getPvPers() {
        return this.pvpers;
    }

    public void addPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        if (!playersInCombat.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You have entered combat!");
        }
        playersInCombat.put(uuid, System.currentTimeMillis());
    }

    /**
     * Used on death!
     */
    public void removePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        player.sendMessage(ChatColor.GREEN + "You have left combat!");
        playersInCombat.remove(uuid);
    }
}
