package com.runicrealms.plugin.character;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharacterManager implements Listener {

    private static Map<UUID, Integer> selectedCharacters = new HashMap<UUID, Integer>();

    public static Map<UUID, Integer> getSelectedCharacters() {
        return selectedCharacters;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerCache cache = RunicCore.getCacheManager().getPlayerCache(event.getPlayer().getUniqueId());
        if (cache != null) {
            CharacterQuitEvent characterQuitEvent = new CharacterQuitEvent(cache, event.getPlayer());
            Bukkit.getPluginManager().callEvent(characterQuitEvent);
        }
    }

}
