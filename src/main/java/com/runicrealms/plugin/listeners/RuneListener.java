package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

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
                    pl.getInventory().setItem(0, RunicItemsAPI.generateItemFromTemplate(RUNE_TEMPLATE_ID).generateItem());
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 2L);
    }

    // opens the rune editor
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
        if (!(e.getClickedInventory().getType().equals(InventoryType.PLAYER))) return;

        e.setCancelled(true);
        pl.openInventory(RunicCoreAPI.runeGUI(pl).getInventory());
    }
}
