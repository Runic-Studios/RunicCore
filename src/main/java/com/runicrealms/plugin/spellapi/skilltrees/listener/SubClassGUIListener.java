package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SubClassGUI;
import com.runicrealms.plugin.utilities.GUIUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class SubClassGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        /*
        Preliminary checks
         */
        if (e.getClickedInventory() == null) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof SubClassGUI)) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            e.setCancelled(true);
            return;
        }
        SubClassGUI subClassGUI = (SubClassGUI) e.getClickedInventory().getHolder();
        if (!e.getWhoClicked().equals(subClassGUI.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (subClassGUI.getInventory().getItem(e.getRawSlot()) == null) return;

        ItemStack item = e.getCurrentItem();
        Material material = item.getType();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        e.setCancelled(true);

        if (material == GUIUtil.backButton().getType())
            player.openInventory(new RuneGUI(player).getInventory());
        else if (e.getRawSlot() == 11) // subclass 1
            player.openInventory(RunicCoreAPI.skillTreeGUI(player, SkillTreePosition.FIRST).getInventory());
        else if (e.getRawSlot() == 13) // subclass 2
            player.openInventory(RunicCoreAPI.skillTreeGUI(player, SkillTreePosition.SECOND).getInventory());
        else // subclass 3
            player.openInventory(RunicCoreAPI.skillTreeGUI(player, SkillTreePosition.THIRD).getInventory());
    }
}
