package com.runicrealms.plugin.item.rune;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.ItemGUI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class RuneListener implements Listener {

    // opens the rune editor
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player pl = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();

        // only listen for the rune slot
        if (itemslot != 1) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (pl.getInventory().getItem(1) == null) return;
        ItemStack rune = pl.getInventory().getItem(1);

        ItemMeta meta = rune.getItemMeta();
        if (meta == null) return;
        int durability = ((Damageable) meta).getDamage();

        // only activate in survival mode to save builders the headache
        if (pl.getGameMode() != GameMode.SURVIVAL) return;

        // only listen for a player inventory
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getType().equals(InventoryType.PLAYER))) return;

        e.setCancelled(true);

        // cancel editor in combat
        if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(pl.getUniqueId())) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You can't open that in combat!");
            return;
        }

        // open the rune editor
        pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
        ItemGUI menu = RuneGUI.runeEditor(pl, rune, durability);
        menu.open(pl);
    }

    // cancel rune swapping
    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent swapevent) {

        Player pl = swapevent.getPlayer();
        int slot = pl.getInventory().getHeldItemSlot();

        if (slot == 1) {
            swapevent.setCancelled(true);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.GRAY + "You cannot perform this action in this slot.");
        }
    }
}
