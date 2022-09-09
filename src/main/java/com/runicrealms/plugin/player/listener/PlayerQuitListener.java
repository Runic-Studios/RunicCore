package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.character.api.CharacterHasQuitEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.redis.RedisUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    public static final String DATA_SAVING_KEY = "isSavingData";

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        e.setQuitMessage("");
        player.setWalkSpeed(0.2f);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onCharacterQuit(CharacterQuitEvent event) {
        Bukkit.getLogger().info("player can't join now");
        event.getJedis().set(event.getPlayer().getUniqueId() + ":" + DATA_SAVING_KEY, "true");
        event.getJedis().expire(event.getPlayer().getUniqueId() + ":" + DATA_SAVING_KEY, 30);
    }

    @EventHandler
    public void onCharacterHasQuit(CharacterHasQuitEvent event) {
        Bukkit.getLogger().info("player can join again!");
        RedisUtil.removeFromRedis(event.getCharacterQuitEvent().getJedis(), event.getPlayer().getUniqueId() + ":" + PlayerQuitListener.DATA_SAVING_KEY);
        event.getCharacterQuitEvent().close(); // close all jedis resources
    }

    /**
     * Insurance to prevent player from being locked out
     */
    @EventHandler
    public void onMongoSave(MongoSaveEvent event) {
        Bukkit.getLogger().info("player can join again!");
        RedisUtil.removeFromRedis(event.getJedis(), event.getUuid() + ":" + PlayerQuitListener.DATA_SAVING_KEY);
    }
}
