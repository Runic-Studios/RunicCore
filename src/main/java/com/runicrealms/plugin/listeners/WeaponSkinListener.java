package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.runicitems.RunicItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Handles unskinning a weapon when a player dies/drops it
 */
public class WeaponSkinListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (RunicItems.getWeaponSkinAPI().weaponSkinActive(event.getPlayer(), event.getItemDrop().getItemStack().getType())) {
            event.getItemDrop().setItemStack(RunicItems.getWeaponSkinAPI().disableSkin(event.getItemDrop().getItemStack()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRunicDeath(RunicDeathEvent event) {
        event.getVictim().getInventory().forEach(RunicItems.getWeaponSkinAPI()::disableSkin);
    }

}
