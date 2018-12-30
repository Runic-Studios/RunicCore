package us.fortherealm.plugin.healthbars;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;

import java.util.HashMap;
import java.util.UUID;

public class CombatManager {

    private HashMap<UUID, Long> playersInCombat;
    private Main plugin = Main.getInstance();
    private static final int COMBAT_DURATION = 10;

    public CombatManager() {
        this.playersInCombat = new HashMap<>();
        this.startCombatTask();
    }

    public int getCombatDuration() { return COMBAT_DURATION; }
    public HashMap<UUID, Long> getPlayersInCombat() {
        return this.playersInCombat;
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
                            online.sendMessage(ChatColor.GREEN + "You have left combat!");
                        }
                    }
                }
            }
        }.runTaskTimer(this.plugin, 0, 20);
    }
}
