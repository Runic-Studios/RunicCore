package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.runicitems.RunicItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Handles unskinning a weapon when a player dies/drops it
 */
public class WeaponSkinListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;
        event.getItemDrop().setItemStack(RunicItems.getWeaponSkinAPI().disableSkin(event.getItemDrop().getItemStack()));
    }

    // Handled directly in death listener
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onRunicDeath(RunicDeathEvent event) {
//        event.getVictim().getInventory().forEach(RunicItems.getWeaponSkinAPI()::disableSkin);
//    }

    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        event.getPlayer().getInventory().forEach((item) -> RunicItems.getWeaponSkinAPI().disableDisallowedSkin(event.getPlayer(), item));
    }

}
