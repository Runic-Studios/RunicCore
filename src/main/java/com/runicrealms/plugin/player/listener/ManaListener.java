package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.player.RegenManager;
import com.runicrealms.runicitems.Stat;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
        double wisdomBoost = newMaxMana * (Stat.getMaxManaMult() * RunicCore.getStatAPI().getPlayerWisdom(player.getUniqueId()));
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

    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        // delay by 1 tick to calculate new armor values, not old
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateMaxMana(player);
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterLoadedEvent event) {
        Player player = event.getPlayer();
        // Set their mana to their maxMana on load
        int maxMana = calculateMaxMana(player);
        if (maxMana == 0) {
            maxMana = (int) (RegenManager.getBaseMana() + (RunicCore.getRegenManager().getManaPerLv(player) * player.getLevel()));
        }
        // Store player's current mana
        RunicCore.getRegenManager().getCurrentManaList().put(player.getUniqueId(), maxMana);
    }

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {
        if (!RunicCore.getCharacterAPI().getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
            return; // ignore the change from PlayerJoinEvent
        Player player = event.getPlayer();
        if (player.getLevel() > RegenManager.getBaseMana()) return;
        calculateMaxMana(player);
    }

    /**
     * Updates mana on offhand equip
     */
    @EventHandler
    public void onOffhandEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (event.getClickedInventory() == null) return;
        if (event.getSlot() != 40) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
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
    public void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                calculateMaxMana(player);
            }
        }.runTaskLater(RunicCore.getInstance(), 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onSpellCastEvent(SpellCastEvent event) {
        int manaCost = event.getSpell().getManaCost();
        int currentMana = RunicCore.getRegenManager().getCurrentManaList().get(event.getCaster().getUniqueId());
        if (currentMana < manaCost) {
            event.setCancelled(true);
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.5f);
            event.getCaster().sendMessage(ChatColor.RED + "You don't have enough mana to cast " + event.getSpell().getName() + "!");
        }
    }
}
