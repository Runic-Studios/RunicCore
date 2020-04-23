package com.runicrealms.plugin.player.mana;

import com.codingforcookies.armorequip.ArmorEquipEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

/**
 *  Updates player mana on login and upon armor equip
 */
public class ManaListener implements Listener {

    @EventHandler
    public void onJoin(CharacterLoadEvent e) {

        PlayerCache playerCache = e.getPlayerCache(); // grab right from event to prevent null issues

        Player pl = e.getPlayer();

        // set their mana to their maxMana on login
        int maxMana = playerCache.getMaxMana();
        if (maxMana == 0) {
            maxMana = RunicCore.getManaManager().getBaseMana()+(RunicCore.getManaManager().getManaPerLv(pl)*pl.getLevel());
            RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).setMaxMana(maxMana);
        }
        // store player's current mana
        RunicCore.getManaManager().getCurrentManaList().put(pl.getUniqueId(), maxMana);
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
        }.runTaskLater(RunicCore.getInstance(), 1L);
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
            }.runTaskLater(RunicCore.getInstance(), 1L);
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
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {
        Player pl = e.getPlayer();
        if (pl.getLevel() > RunicCore.getManaManager().getBaseMana()) return;
        calculateMana(pl);
        int maxMana = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getMaxMana();
        RunicCore.getManaManager().getCurrentManaList().put(pl.getUniqueId(), maxMana);
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
        int newMaxMana = RunicCore.getManaManager().getBaseMana() + (RunicCore.getManaManager().getManaPerLv(pl) * pl.getLevel()) + totalItemManaBoost;
        RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).setMaxMana(newMaxMana);

        int maxMana = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getMaxMana();
        int currentMana = RunicCore.getManaManager().getCurrentManaList().get(pl.getUniqueId());
        if (currentMana > maxMana) {
            RunicCore.getManaManager().getCurrentManaList().put(pl.getUniqueId(), maxMana);
        }
    }
}
