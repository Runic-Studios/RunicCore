package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Removes helper objects used to model data from memory after they are used
 */
public class ModelListener implements Listener {

    @EventHandler
    public void onCharacterSelect(CharacterLoadedEvent e) {
        RunicCore.getDatabaseManager().getPlayerDataMap().remove(e.getPlayer().getUniqueId());
    }

}
