package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.rdb.event.CharacterSelectEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsManager implements Listener {
    private final Map<UUID, SettingsData> settingsDataMap = new HashMap<>();

    public SettingsManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @EventHandler
    public void onSelect(CharacterSelectEvent event) {
        settingsDataMap.put(event.getPlayer().getUniqueId(), new SettingsData(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        settingsDataMap.remove(event.getPlayer().getUniqueId());
    }

    public SettingsData getSettingsData(UUID owner) {
        return settingsDataMap.get(owner);
    }

}
