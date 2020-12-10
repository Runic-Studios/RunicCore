package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SubClassGUI;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SubClassGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        /*
        Preliminary checks
         */
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof SubClassGUI)) return;
        SubClassGUI subClassGUI = (SubClassGUI) e.getClickedInventory().getHolder();
        if (!e.getWhoClicked().equals(subClassGUI.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        Player pl = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (subClassGUI.getInventory().getItem(e.getRawSlot()) == null) return;

        ItemStack item = e.getCurrentItem();
        Material material = item.getType();

        pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        e.setCancelled(true);

        if (material == GUIUtil.backButton().getType())
            pl.openInventory(new RuneGUI(pl).getInventory());
        else if (e.getRawSlot() == 11) // sub-class 1
            pl.openInventory(RunicCoreAPI.skillTreeGUI(pl, 1).getInventory());
        else if (e.getRawSlot() == 13) // sub-class 2
            pl.openInventory(RunicCoreAPI.skillTreeGUI(pl, 2).getInventory());
        else // sub-class 3
            pl.openInventory(RunicCoreAPI.skillTreeGUI(pl, 3).getInventory());
    }
}
