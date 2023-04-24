package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemGeneric;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class RuneListener implements Listener {

    private static final String RUNE_TEMPLATE_ID = "rune";

    /**
     * Give players the rune
     */
    @EventHandler
    public void onCharacterLoad(CharacterLoadedEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getInventory().getItem(0) == null
                        || (player.getInventory().getItem(0) != null
                        && player.getInventory().getItem(0).getType() != Material.POPPED_CHORUS_FRUIT)) {
                    RunicItemGeneric runicItemGeneric = (RunicItemGeneric) RunicItemsAPI.generateItemFromTemplate(RUNE_TEMPLATE_ID);
                    player.getInventory().setItem(0, runicItemGeneric.generateItem());
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // event runs LAST, not first
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.isCancelled()) return;
        Player player = (Player) event.getWhoClicked();
        int itemSlot = event.getSlot();
        if (itemSlot != 0) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (player.getInventory().getItem(0) == null) return;
        ItemStack rune = player.getInventory().getItem(0);
        if (rune == null) return;
        ItemMeta meta = rune.getItemMeta();
        if (meta == null) return;

        // only activate in survival mode to save builders the headache
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // only listen for a player inventory
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() != InventoryType.PLAYER) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRuneUse(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY)
            return;
        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot != 0) return;
        if (player.getInventory().getItem(0) == null) return;
        ItemStack rune = player.getInventory().getItem(0);
        if (rune == null) return;

        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        player.openInventory(new RuneGUI(player).getInventory());
    }
}
