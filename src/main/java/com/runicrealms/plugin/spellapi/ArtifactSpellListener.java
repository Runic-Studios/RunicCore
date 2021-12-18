package com.runicrealms.plugin.spellapi;

import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.runicitems.item.event.RunicItemArtifactTriggerEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ArtifactSpellListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onArtifactSpellUse(RunicItemArtifactTriggerEvent e) {
        if (e.getArtifactSpellToCast() == null) return;
        Bukkit.getPluginManager().callEvent(new SpellCastEvent(e.getPlayer(), e.getArtifactSpellToCast()));
    }
}
