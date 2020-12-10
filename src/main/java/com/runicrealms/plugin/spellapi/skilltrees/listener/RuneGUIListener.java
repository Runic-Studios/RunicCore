package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SubClassGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class RuneGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        /*
        Preliminary checks
         */
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof RuneGUI)) return;
        RuneGUI runeGUI = (RuneGUI) e.getClickedInventory().getHolder();
        if (!e.getWhoClicked().equals(runeGUI.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        Player pl = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (runeGUI.getInventory().getItem(e.getRawSlot()) == null) return;

        ItemStack item = e.getCurrentItem();
        Material material = item.getType();

        pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        e.setCancelled(true);

        if (material == runeGUI.skillTreeButton().getType())
            pl.openInventory(new SubClassGUI(pl).getInventory());
        else if (material == RuneGUI.spellEditorButton().getType())
            Bukkit.broadcastMessage("spell selector here");
        else
            pl.closeInventory();
    }
}
