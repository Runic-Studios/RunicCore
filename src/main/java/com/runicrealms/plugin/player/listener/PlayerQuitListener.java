package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterHasQuitEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class PlayerQuitListener implements Listener {
    public static final String DATA_SAVING_KEY = "isSavingData";
    /*
    Player is prevented from joining network while data is saving,
    or for a max of 30 seconds
     */
    private static final int DATA_LOCKOUT_TIMEOUT = 30;

    @EventHandler
    public void onCharacterHasQuit(CharacterHasQuitEvent event) {
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                jedis.del(database + ":" + uuid + ":" + PlayerQuitListener.DATA_SAVING_KEY);
                if (!event.getPlayer().isOnline()) { // Insurance
                    RunicCore.getPlayerDataAPI().getCorePlayerDataMap().remove(event.getPlayer().getUniqueId());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onCharacterQuit(CharacterQuitEvent event) {
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        UUID uuid = event.getPlayer().getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                jedis.set(database + ":" + uuid + ":" + DATA_SAVING_KEY, "true");
                jedis.expire(database + ":" + uuid + ":" + DATA_SAVING_KEY, DATA_LOCKOUT_TIMEOUT);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage("");
        player.setWalkSpeed(0.2f);
    }
}
