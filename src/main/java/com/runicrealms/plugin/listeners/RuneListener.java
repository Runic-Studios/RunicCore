package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemGeneric;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

import java.util.Objects;
import java.util.UUID;

public class RuneListener implements Listener {

    private static final String RUNE_TEMPLATE_ID = "rune";

    /*
     * Give new players the rune
     */
    @EventHandler
    public void onCharacterLoad(CharacterLoadEvent e) {
        Player pl = e.getPlayer();
        if (pl.getGameMode() != GameMode.SURVIVAL) return;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pl.getInventory().getItem(0) == null
                        || (pl.getInventory().getItem(0) != null
                        && pl.getInventory().getItem(0).getType() != Material.POPPED_CHORUS_FRUIT)) {
                    RunicItemGeneric runicItemGeneric = (RunicItemGeneric) RunicItemsAPI.generateItemFromTemplate(RUNE_TEMPLATE_ID);
                    pl.getInventory().setItem(0, runicItemGeneric.generateItem());
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 2L);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // event runs LAST, not first
    public void onInventoryClick(InventoryClickEvent e) {

        if (e.isCancelled()) return;
        Player pl = (Player) e.getWhoClicked();
        int itemSlot = e.getSlot();
        if (itemSlot != 0) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (pl.getInventory().getItem(0) == null) return;
        ItemStack rune = pl.getInventory().getItem(0);

        ItemMeta meta = Objects.requireNonNull(rune).getItemMeta();
        if (meta == null) return;

        // only activate in survival mode to save builders the headache
        if (pl.getGameMode() != GameMode.SURVIVAL) return;

        // only listen for a player inventory
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.PLAYER) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onRuneUse(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot != 0) return;
        if (player.getInventory().getItem(0) == null) return;
        ItemStack rune = player.getInventory().getItem(0);
        if (rune == null) return;

        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // prevent player's from teleporting in combat
        if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You can't use that in combat!");
            return;
        }

        player.openInventory(RunicCoreAPI.runeGUI(player).getInventory());
    }
}
