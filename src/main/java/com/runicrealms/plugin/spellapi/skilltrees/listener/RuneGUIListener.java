package com.runicrealms.plugin.spellapi.skilltrees.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
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
    public void onInventoryClick(InventoryClickEvent e) {

        /*
        Preliminary checks
         */
        if (e.getClickedInventory() == null) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof RuneGUI)) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            e.setCancelled(true);
            return;
        }
        RuneGUI runeGUI = (RuneGUI) e.getClickedInventory().getHolder();
        if (!e.getWhoClicked().equals(runeGUI.getPlayer())) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
            return;
        }
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (runeGUI.getInventory().getItem(e.getRawSlot()) == null) return;

        ItemStack item = e.getCurrentItem();
        Material material = item.getType();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        e.setCancelled(true);

        if (material == runeGUI.skillTreeButton().getType())
            player.openInventory(new SubClassGUI(player, RunicCoreAPI.getCharacterSlot(player.getUniqueId())).getInventory());
        else if (material == RuneGUI.spellEditorButton().getType())
            // prevent using the spell editor in combat
            if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(player.getUniqueId()))
                player.sendMessage(ChatColor.RED + "You can't use that in combat!");
            else
                player.openInventory(new SpellEditorGUI(player).getInventory());
        else
            player.closeInventory();
    }
}
