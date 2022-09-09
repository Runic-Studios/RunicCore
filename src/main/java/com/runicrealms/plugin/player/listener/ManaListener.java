package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import com.runicrealms.plugin.player.RegenManager;
import com.runicrealms.runicitems.Stat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Updates player mana on login and upon armor equip
 */
public class ManaListener implements Listener {

    @EventHandler
    public void onJoin(CharacterSelectEvent e) {

        Player player = e.getPlayer();

        // set their mana to their maxMana on login
        int maxMana = calculateMaxMana(player);
        if (maxMana == 0) {
            maxMana = (int) (RegenManager.getBaseMana() + (RunicCore.getRegenManager().getManaPerLv(player) * player.getLevel()));
        }
        // store player's current mana
        RunicCore.getRegenManager().getCurrentManaList().put(player.getUniqueId(), maxMana);
    }

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {
        Player player = e.getPlayer();
        // delay by 1 tick to calculate new armor values, not old
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateMaxMana(player);
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    /**
     * Updates mana on offhand equip
     */
    @EventHandler
    public void onOffhandEquip(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (e.getClickedInventory() == null) return;
        if (e.getSlot() != 40) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    calculateMaxMana(player);
                }
            }.runTaskLater(RunicCore.getInstance(), 1L);
        }
    }

    /**
     * Updates mana on off-hand swap
     */
    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateMaxMana(player);
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {
        if (!RunicCoreAPI.getLoadedCharacters().contains(e.getPlayer().getUniqueId()))
            return; // ignore the change from PlayerJoinEvent
        Player player = e.getPlayer();
        if (player.getLevel() > RegenManager.getBaseMana()) return;
        calculateMaxMana(player);
    }

    /**
     * Calculates the total mana for the given player
     *
     * @param player to calculate mana for
     */
    public static int calculateMaxMana(Player player) {
        int maxMana;
        // recalculate max mana based on player level
        int newMaxMana = (int) (RegenManager.getBaseMana() + (RunicCore.getRegenManager().getManaPerLv(player) * player.getLevel()));
        // grab extra mana from wisdom
        double wisdomBoost = newMaxMana * (Stat.getMaxManaMult() * RunicCoreAPI.getPlayerWisdom(player.getUniqueId()));
        maxMana = (int) (newMaxMana + wisdomBoost);

        // fix current mana if it is now too high
        int currentMana;
        try {
            currentMana = RunicCore.getRegenManager().getCurrentManaList().get(player.getUniqueId());
        } catch (NullPointerException e) {
            currentMana = 0;
        }
        if (currentMana > maxMana) {
            RunicCore.getRegenManager().getCurrentManaList().put(player.getUniqueId(), maxMana);
        }
        return maxMana;
    }
}
