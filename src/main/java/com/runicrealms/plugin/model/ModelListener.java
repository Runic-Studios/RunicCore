package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Removes helper objects used to model data from memory after they are used
 */
public class ModelListener implements Listener {

    // todo: create 'characterselectevent' and 'characterloadedevent' to prevent race conditions
    @EventHandler
    public void onCharacterSelect(CharacterLoadEvent e) {
        // load guild data here
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> {
                    RunicCore.getCacheManager().getPlayerDataMap().remove(e.getPlayer().getUniqueId());
                }, 5L);
    }

}
