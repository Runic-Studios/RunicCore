package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.GUIUtil;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SubClassGUI;
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
    public void onInventoryClick(InventoryClickEvent event) {

        /*
        Preliminary checks
         */
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof SubClassGUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        SubClassGUI subClassGUI = (SubClassGUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(subClassGUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (subClassGUI.getInventory().getItem(event.getRawSlot()) == null) return;

        ItemStack item = event.getCurrentItem();
        Material material = item.getType();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (material == GUIUtil.BACK_BUTTON.getType())
            player.openInventory(new RuneGUI(player).getInventory());
        else if (event.getRawSlot() == 11) // subclass 1
            player.openInventory(RunicCore.getSkillTreeAPI().skillTreeGUI(player, SkillTreePosition.FIRST).getInventory());
        else if (event.getRawSlot() == 13) // subclass 2
            player.openInventory(RunicCore.getSkillTreeAPI().skillTreeGUI(player, SkillTreePosition.SECOND).getInventory());
        else // subclass 3
            player.openInventory(RunicCore.getSkillTreeAPI().skillTreeGUI(player, SkillTreePosition.THIRD).getInventory());
    }
}
