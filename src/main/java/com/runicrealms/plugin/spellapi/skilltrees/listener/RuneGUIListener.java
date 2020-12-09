package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

        e.setCancelled(true);

        if (material == RuneGUI.spellEditorButton().getType())
            Bukkit.broadcastMessage("spell selector here");
        else if (material == RuneGUI.skillTreeButton(ClassEnum.MAGE).getType())
            pl.openInventory(RunicCoreAPI.skillTreeGUI(pl).getInventory());
        else
            pl.closeInventory();
    }
}
