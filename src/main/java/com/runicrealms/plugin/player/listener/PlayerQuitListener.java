package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.character.api.CharacterHasQuitEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.redis.RedisUtil;
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
        event.getJedis().set(event.getPlayer().getUniqueId() + ":" + DATA_SAVING_KEY, "true");
        event.getJedis().expire(event.getPlayer().getUniqueId() + ":" + DATA_SAVING_KEY, 30);
    }

    @EventHandler
    public void onCharacterHasQuit(CharacterHasQuitEvent event) {
        RedisUtil.removeFromRedis(event.getCharacterQuitEvent().getJedis(), event.getPlayer().getUniqueId() + ":" + PlayerQuitListener.DATA_SAVING_KEY);
        event.getCharacterQuitEvent().close(); // close all jedis resources
    }
}
