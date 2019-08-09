package com.runicrealms.plugin.player;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;

import java.util.ArrayList;

/**
 *  Updates player mana on login and upon armor equip
 */
public class ManaListener implements Listener {

    private Plugin plugin = RunicCore.getInstance();
    private ManaManager manaManager = RunicCore.getManaManager();

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {

        Player pl = e.getPlayer();

        // set their mana to their maxMana on login
        // create the field if it doesn't exist
        int maxMana = plugin.getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
        if (maxMana == 0) {
            maxMana = 50+(manaManager.getManaPerLevel()*pl.getLevel());
            plugin.getConfig().set(pl.getUniqueId() + ".info.maxMana", maxMana);
            plugin.saveConfig();
            plugin.reloadConfig();
        }
        // store player's current mana
        manaManager.getCurrentManaList().put(pl.getUniqueId(), maxMana);
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {

        Player pl = e.getPlayer();

        // delay by 1 tick to calculate new armor values, not old
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateMana(pl);
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * Updates mana on offhand equip
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
                    calculateMana(pl);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    /**
     * Updates mana on off-hand swap
     */
    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent e) {
        Player pl = e.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateMana(pl);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {
        Player pl = e.getPlayer();
        if (pl.getLevel() > 50) return;
        calculateMana(pl);
        int maxMana = plugin.getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
        manaManager.getCurrentManaList().put(pl.getUniqueId(), maxMana);
    }

    private void calculateMana(Player pl) {

        // grab player's armor, offhand
        ArrayList<ItemStack> armorAndOffhand = GearScanner.armorAndOffHand(pl);

        int totalItemManaBoost = 0;

        // calculate the player's total mana boost
        for (ItemStack item : armorAndOffhand) {
            int itemManaBoost = (int) AttributeUtil.getCustomDouble(item, "custom.manaBoost");
            totalItemManaBoost = totalItemManaBoost + itemManaBoost;
        }

        // update stored mana in config, update scoreboard
        int newMaxMana = 50 + (manaManager.getManaPerLevel() * pl.getLevel()) + totalItemManaBoost;
        plugin.getConfig().set(pl.getUniqueId() + ".info.maxMana", newMaxMana);
        saveConfig(pl);

        int maxMana = plugin.getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
        int currentMana = manaManager.getCurrentManaList().get(pl.getUniqueId());
        if (currentMana > maxMana) {
            manaManager.getCurrentManaList().put(pl.getUniqueId(), maxMana);
            RunicCore.getScoreboardHandler().updateSideInfo(pl);
        }
    }

    private void saveConfig(Player pl) {
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
        RunicCore.getScoreboardHandler().updateSideInfo(pl);
    }
}
