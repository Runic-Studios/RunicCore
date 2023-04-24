package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SpellEditorGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SubClassGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class RuneGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        /*
        Preliminary checks
         */
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof RuneGUI)) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        RuneGUI runeGUI = (RuneGUI) event.getClickedInventory().getHolder();
        if (!event.getWhoClicked().equals(runeGUI.getPlayer())) {
            event.setCancelled(true);
            event.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null) return;
        if (runeGUI.getInventory().getItem(event.getRawSlot()) == null) return;

        ItemStack item = event.getCurrentItem();
        Material material = item.getType();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        event.setCancelled(true);

        if (material == runeGUI.skillTreeButton().getType())
            player.openInventory(new SubClassGUI(player).getInventory());
        else if (material == RuneGUI.SPELL_EDITOR_BUTTON.getType())
            // prevent using the spell editor in combat
            if (RunicCore.getCombatAPI().isInCombat(player.getUniqueId()))
                player.sendMessage(ChatColor.RED + "You can't use that in combat!");
            else
                player.openInventory(new SpellEditorGUI(player).getInventory());
        else
            player.closeInventory();
    }
}
