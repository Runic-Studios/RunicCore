package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SpellEditorGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SpellGUI;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SpellEditorGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        /*
        Preliminary checks
         */
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof SpellEditorGUI)) return;
        SpellEditorGUI spellEditorGUI = (SpellEditorGUI) e.getClickedInventory().getHolder();
        if (!e.getWhoClicked().equals(spellEditorGUI.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        Player pl = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (spellEditorGUI.getInventory().getItem(e.getRawSlot()) == null) return;

        ItemStack item = e.getCurrentItem();
        Material material = item.getType();

        pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        e.setCancelled(true);

        if (material == GUIUtil.backButton().getType())
            pl.openInventory(new RuneGUI(pl).getInventory());
        else if (material == SpellEditorGUI.spellButtonHotbarOne().getType())
            pl.openInventory(new SpellGUI(pl).getInventory());
        else
            pl.closeInventory();
    }
}
