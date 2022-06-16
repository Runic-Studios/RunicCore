package com.runicrealms.plugin.scoreboard;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ScoreboardListener implements Listener {

    /**
     * Updates health and scoreboard on armor equip
     */
    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
    }

    /**
     * Updates health and scoreboard on offhand equip
     */
    @EventHandler
    public void onOffhandEquip(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (e.getClickedInventory() == null) return;
        if (e.getSlot() != 40) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
        }
    }

    /**
     * Updates health and scoreboard on off-hand swap
     */
    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
    }

    /**
     * Updates health and scoreboard on level change
     */
    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
    }

}
