package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.RunicNpcs;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NpcListener implements Listener {

    public NpcListener() {
        Bukkit.getPluginManager().registerEvents(new NpcListener(), RunicCore.getInstance());
    }

    /**
     * For load all NPCs for player on character load
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCharacterLoad(CharacterLoadedEvent event) {
        RunicNpcs.getAPI().updateNpcsForPlayer(event.getPlayer());
    }

}
