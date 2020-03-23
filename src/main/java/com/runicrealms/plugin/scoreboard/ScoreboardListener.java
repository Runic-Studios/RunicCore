package com.runicrealms.plugin.scoreboard;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreboardListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        // only listen for players
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getEntity().hasMetadata("NPC")) return;
        Player pl = (Player) e.getEntity();
        // null check
        if (pl.getScoreboard() == null) { return; }
        updateHealth(pl);
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {
        //only listen for players
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getEntity().hasMetadata("NPC")) return;
        Player pl = (Player) e.getEntity();
        // null check
        if (pl.getScoreboard() == null) { return; }
        updateHealth(pl);
    }

    /**
     * Updates health on armor equip
     */
    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {
        Player pl = e.getPlayer();
        // null check
        if (pl.getScoreboard() == null) { return; }

        new BukkitRunnable() {
            @Override
            public void run() {
                HealthUtils.setPlayerMaxHealth(pl);
                updateHealth(pl);
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    /**
     * Updates health on offhand equip
     */
    @EventHandler
    public void onOffhandEquip(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player pl = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (e.getClickedInventory() == null) return;
        if (e.getSlot() != 40) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    HealthUtils.setPlayerMaxHealth(pl);
                    updateHealth(pl);
                }
            }.runTaskLater(RunicCore.getInstance(), 1L);
        }
    }

    /**
     * Updates health on off-hand swap
     */
    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent e) {
        Player pl = e.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                HealthUtils.setPlayerMaxHealth(pl);
                updateHealth(pl);
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    private void updateHealth(Player pl) {
        // update health bar and scoreboard
        new BukkitRunnable() {
            @Override
            public void run() {
                RunicCore.getScoreboardHandler().updateSideInfo(pl);
                RunicCore.getScoreboardHandler().updateHealthbar(pl);
            }
        }.runTaskLater(RunicCore.getInstance(), 1);
    }
}
