package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterHasQuitEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onCharacterHasQuit(CharacterHasQuitEvent event) {
        RunicDatabase.getAPI().getDataAPI().preventLogin(event.getPlayer().getUniqueId());
        // Removes the CorePlayerData from the in-memory structure (should ALWAYS happen before they are allowed back on the server)
        RunicCore.getPlayerDataAPI().getCorePlayerDataMap().remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onCharacterQuit(CharacterQuitEvent event) {
        RunicDatabase.getAPI().getDataAPI().preventLogin(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage("");
        player.setWalkSpeed(0.2f);
    }
}
