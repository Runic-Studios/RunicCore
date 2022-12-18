package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.CombatAPI;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class manages combat against mobs and players and handles each differently,
 * which is why we need both a HashMap and a List.
 */
public class CombatManager implements CombatAPI, Listener {

    private static final double COMBAT_DURATION = 10;
    private final HashMap<UUID, Long> playersInCombat;

    public CombatManager() {
        this.playersInCombat = new HashMap<>();
        this.startCombatTask();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @Override
    public void enterCombat(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        assert player != null;
        if (!playersInCombat.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You have entered combat!");
        }
        playersInCombat.put(uuid, System.currentTimeMillis());
    }

    @Override
    public void giveCombatExp(Player player, int exp) {
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) {
            PlayerLevelUtil.giveExperience(player, exp, jedis);
        }
    }

    @Override
    public boolean isInCombat(UUID uuid) {
        return this.playersInCombat.containsKey(uuid);
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
            for (UUID uuid : RunicCore.getCharacterAPI().getLoadedCharacters()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                if (playersInCombat.containsKey(uuid)) {
                    if (System.currentTimeMillis() - playersInCombat.get(uuid) >= (COMBAT_DURATION * 1000)) {
                        LeaveCombatEvent leaveCombatEvent = new LeaveCombatEvent(player);
                        Bukkit.getPluginManager().callEvent(leaveCombatEvent);
                    }
                }
            }
        }, 0, 20L);
    }
}
